package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.skillEvidence;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class aiSkillExtractor implements skillExtractor {
    private final boolean aiEnabled;
    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int timeoutMs;
    private final boolean useResponseFormat;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public aiSkillExtractor(@Value("${app.ai.enabled:false}") boolean aiEnabled,
                            @Value("${app.ai.api-key:}") String apiKey,
                            @Value("${app.ai.base-url:https://api.openai.com}") String baseUrl,
                            @Value("${app.ai.model:gpt-4o-mini}") String model,
                            @Value("${app.ai.timeout-ms:15000}") int timeoutMs,
                            @Value("${app.ai.use-response-format:true}") boolean useResponseFormat,
                            ObjectMapper objectMapper) {
        this.aiEnabled = aiEnabled;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.timeoutMs = timeoutMs;
        this.useResponseFormat = useResponseFormat;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }

    @Override
    public skillExtractionResult extractSkills(String resumeText, String githubLikeText) {
        if (!aiEnabled) {
            throw new IllegalStateException("AI skill extraction is disabled");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI API key is missing");
        }

        try {
            String requestBody = buildRequestBody(resumeText, githubLikeText);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(baseUrl) + "/v1/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("AI API error status: " + response.statusCode());
            }

            List<skillEvidence> evidences = extractSkillEvidenceFromResponse(response.body());
            if (evidences.isEmpty()) {
                throw new IllegalStateException("AI produced no usable skills");
            }

            List<String> skills = evidences.stream().map(skillEvidence::getSkill).toList();
            return new skillExtractionResult(skills, evidences, "AI_ASSISTED", null);
        } catch (Exception ex) {
            throw new IllegalStateException("AI extraction failed: " + ex.getMessage(), ex);
        }
    }

    private String buildRequestBody(String resumeText, String githubLikeText) throws Exception {
        String systemPrompt = "Extract only technical skills and confidence. "
                + "Return JSON object only with this schema: "
                + "{\"skills\":[{\"name\":\"Java\",\"confidence\":0.93}]}. "
                + "Confidence must be between 0 and 1.";

        String userPrompt = "Resume Text:\n" + safeText(resumeText)
                + "\n\nGitHub-Like Text:\n" + safeText(githubLikeText)
                + "\n\nReturn only the JSON object.";

        JsonNode root = objectMapper.createObjectNode();
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("model", model);
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("temperature", 0.1);

        com.fasterxml.jackson.databind.node.ArrayNode messages = objectMapper.createArrayNode();
        messages.add(objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt));
        messages.add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userPrompt));
        ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("messages", messages);

        if (useResponseFormat) {
            com.fasterxml.jackson.databind.node.ObjectNode responseFormat = objectMapper.createObjectNode();
            responseFormat.put("type", "json_object");
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("response_format", responseFormat);
        }

        return objectMapper.writeValueAsString(root);
    }

    private List<skillEvidence> extractSkillEvidenceFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull()) {
            return List.of();
        }

        String content = contentNode.asText("");
        String jsonPayload = extractJsonObject(content);
        JsonNode parsed = objectMapper.readTree(jsonPayload);
        JsonNode skillsNode = parsed.path("skills");
        if (!skillsNode.isArray()) {
            return List.of();
        }

        Map<String, skillEvidence> bySkill = new LinkedHashMap<>();
        for (JsonNode node : skillsNode) {
            String skill = "";
            double confidence = 0.85;

            if (node.isTextual()) {
                skill = normalizeSkill(node.asText(""));
            } else if (node.isObject()) {
                skill = normalizeSkill(node.path("name").asText(""));
                if (node.has("confidence")) {
                    confidence = clampConfidence(node.path("confidence").asDouble(0.85));
                }
            }

            if (!skill.isBlank()) {
                skillEvidence existing = bySkill.get(skill.toLowerCase(Locale.ROOT));
                skillEvidence current = new skillEvidence(skill, "AI_MODEL", confidence);
                if (existing == null || current.getConfidence() > existing.getConfidence()) {
                    bySkill.put(skill.toLowerCase(Locale.ROOT), current);
                }
            }
            if (bySkill.size() >= 50) {
                break;
            }
        }
        return new ArrayList<>(bySkill.values());
    }

    private double clampConfidence(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    private String extractJsonObject(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        throw new IllegalStateException("AI response did not contain JSON object");
    }

    private String normalizeSkill(String raw) {
        String cleaned = raw == null ? "" : raw.trim();
        if (cleaned.isBlank()) {
            return "";
        }
        if (cleaned.length() > 60) {
            return "";
        }
        if (!cleaned.matches(".*[A-Za-z].*")) {
            return "";
        }
        return cleaned.replaceAll("\\s+", " ").trim();
    }

    private String safeText(String text) {
        String value = text == null ? "" : text.trim();
        if (value.length() > 6000) {
            return value.substring(0, 6000);
        }
        return value;
    }

    private String trimTrailingSlash(String value) {
        String base = value == null ? "" : value.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base.toLowerCase(Locale.ROOT).startsWith("http") ? base : "https://" + base;
    }
}
