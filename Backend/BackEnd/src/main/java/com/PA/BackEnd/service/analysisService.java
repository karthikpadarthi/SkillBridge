package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.skillPriority;
import com.PA.BackEnd.dto.skillEvidence;
import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.repository.jobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class analysisService {
    private final jobRepository jobRepository;
    private final aiSkillExtractor aiSkillExtractor;
    private final ruleBasedSkillExtractor ruleBasedSkillExtractor;

    public analysisService(jobRepository jobRepository,
                           aiSkillExtractor aiSkillExtractor,
                           ruleBasedSkillExtractor ruleBasedSkillExtractor) {
        this.jobRepository = jobRepository;
        this.aiSkillExtractor = aiSkillExtractor;
        this.ruleBasedSkillExtractor = ruleBasedSkillExtractor;
    }

    public gapAnalysisResponse analyze(analyzeRequest request) {
        String targetRole = request.getTargetRole() == null ? "" : request.getTargetRole().trim();
        Optional<jobRole> roleLookup = jobRepository.findByRoleNameIgnoreCase(targetRole);
        if (roleLookup.isEmpty()) {
            throw new IllegalArgumentException("Unknown target role: " + targetRole);
        }

        jobRole role = roleLookup.get();
        skillExtractionResult extraction = extractWithFallback(request);
        List<String> extractedSkills = normalizeAndDistinct(extraction.getSkills());
        List<skillEvidence> extractedEvidence = normalizeEvidence(extraction.getEvidences(), extractedSkills);
        Set<String> extractedLower = extractedSkills.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        for (String roleSkill : role.getRequiredSkills()) {
            if (extractedLower.contains(roleSkill.toLowerCase(Locale.ROOT))) {
                matchedSkills.add(roleSkill);
            } else {
                missingSkills.add(roleSkill);
            }
        }

        List<String> transferableSkills = request.getCurrentSkills().stream()
                .filter(skill -> !containsIgnoreCase(matchedSkills, skill))
                .collect(Collectors.toList());

        List<skillPriority> prioritizedMissing = rankMissingSkills(missingSkills, role.getSkillDemand());
        List<String> recommendations = prioritizedMissing.stream()
                .map(this::recommendationForSkill)
                .collect(Collectors.toList());

        int coverageScore = role.getRequiredSkills().isEmpty()
                ? 0
                : (int) Math.round((matchedSkills.size() * 100.0) / role.getRequiredSkills().size());

        gapAnalysisResponse response = new gapAnalysisResponse();
        response.setTargetRole(role.getRoleName());
        response.setAnalysisMode(extraction.getMode());
        response.setFallbackReason(extraction.getFallbackReason());
        response.setExtractedSkills(extractedSkills);
        response.setExtractedSkillEvidence(extractedEvidence);
        response.setMatchedSkills(matchedSkills);
        response.setMissingSkills(missingSkills);
        response.setTransferableSkills(transferableSkills);
        response.setPrioritizedMissingSkills(prioritizedMissing);
        response.setRecommendations(recommendations);
        response.setCoverageScore(coverageScore);
        response.setNextStep(buildNextStep(prioritizedMissing));
        response.setGeneratedAt(Instant.now());
        return response;
    }

    private skillExtractionResult extractWithFallback(analyzeRequest request) {
        try {
            skillExtractionResult aiResult = aiSkillExtractor.extractSkills(
                    request.getResumeText(),
                    request.getGithubLikeText()
            );

            if (aiResult.getSkills() == null || aiResult.getSkills().isEmpty()) {
                skillExtractionResult fallback = ruleBasedSkillExtractor.extractSkills(
                        request.getResumeText(),
                        request.getGithubLikeText()
                );
                fallback.setFallbackReason("AI returned no skills. Used deterministic extractor.");
                return fallback;
            }
            return aiResult;
        } catch (Exception ex) {
            skillExtractionResult fallback = ruleBasedSkillExtractor.extractSkills(
                    request.getResumeText(),
                    request.getGithubLikeText()
            );
            fallback.setFallbackReason("AI unavailable: " + ex.getMessage());
            return fallback;
        }
    }

    private List<skillPriority> rankMissingSkills(List<String> missingSkills, Map<String, Integer> demandMap) {
        return missingSkills.stream()
                .map(skill -> {
                    int score = demandMap.getOrDefault(skill, 60);
                    String urgency = score >= 85 ? "HIGH" : score >= 70 ? "MEDIUM" : "LOW";
                    return new skillPriority(skill, score, urgency);
                })
                .sorted(Comparator.comparingInt(skillPriority::getMarketDemandScore).reversed())
                .collect(Collectors.toList());
    }

    private String recommendationForSkill(skillPriority item) {
        return "Build proficiency in " + item.getSkill()
                + " (market demand " + item.getMarketDemandScore() + "/100, urgency " + item.getUrgency() + ").";
    }

    private String buildNextStep(List<skillPriority> prioritizedMissing) {
        if (prioritizedMissing.isEmpty()) {
            return "Start mock interviews for your target role.";
        }
        skillPriority top = prioritizedMissing.get(0);
        return "Start with " + top.getSkill() + " and complete one hands-on project this week.";
    }

    private boolean containsIgnoreCase(List<String> values, String candidate) {
        for (String value : values) {
            if (value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeAndDistinct(List<String> source) {
        if (source == null) {
            return List.of();
        }

        Set<String> dedup = new LinkedHashSet<>();
        for (String item : source) {
            if (item != null && !item.isBlank()) {
                dedup.add(item.trim());
            }
        }
        return new ArrayList<>(dedup);
    }

    private List<skillEvidence> normalizeEvidence(List<skillEvidence> evidences, List<String> normalizedSkills) {
        List<skillEvidence> result = new ArrayList<>();
        if (evidences == null) {
            return result;
        }

        Set<String> allowed = normalizedSkills.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> seen = new LinkedHashSet<>();

        for (skillEvidence evidence : evidences) {
            if (evidence == null || evidence.getSkill() == null) {
                continue;
            }
            String skill = evidence.getSkill().trim();
            String key = skill.toLowerCase(Locale.ROOT);
            if (!allowed.contains(key) || seen.contains(key)) {
                continue;
            }

            double confidence = evidence.getConfidence();
            if (confidence < 0.0) {
                confidence = 0.0;
            } else if (confidence > 1.0) {
                confidence = 1.0;
            }

            String source = evidence.getSource() == null || evidence.getSource().isBlank()
                    ? "UNKNOWN"
                    : evidence.getSource().trim();

            result.add(new skillEvidence(skill, source, Math.round(confidence * 100.0) / 100.0));
            seen.add(key);
        }
        return result;
    }
}
