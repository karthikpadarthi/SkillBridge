package com.PA.BackEnd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class BackEndApplicationTests {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0.14");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("app.jwt.secret", () -> "integration_test_super_secure_jwt_secret_key_2026");
        registry.add("app.ai.enabled", () -> "false");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    void fullSecuredFlow_withHistoryAndMetrics_shouldSucceed() throws Exception {
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String password = "Password@123";

        String registerBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));
        HttpResponse<String> registerResp = postJson("/api/auth/register", registerBody, null);
        assertThat(registerResp.statusCode()).isEqualTo(200);
        JsonNode registerJson = objectMapper.readTree(registerResp.body());
        assertThat(registerJson.path("token").asText()).isNotBlank();
        assertThat(registerJson.path("userId").asText()).isEqualTo(email);

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", email,
                "password", password
        ));
        HttpResponse<String> loginResp = postJson("/api/auth/login", loginBody, null);
        assertThat(loginResp.statusCode()).isEqualTo(200);
        String token = objectMapper.readTree(loginResp.body()).path("token").asText();
        assertThat(token).isNotBlank();

        String analyzeBody = objectMapper.writeValueAsString(Map.of(
                "resumeText", "Built Java REST APIs using Spring Boot and SQL. Worked with Docker and CI/CD.",
                "githubLikeText", "Implemented microservices with Java and Docker.",
                "targetRole", "Backend Engineer",
                "currentSkills", List.of("Git", "Problem Solving")
        ));
        HttpResponse<String> analyzeResp = postJson("/api/analyze", analyzeBody, token);
        assertThat(analyzeResp.statusCode()).isEqualTo(200);
        JsonNode analyzeJson = objectMapper.readTree(analyzeResp.body());
        assertThat(analyzeJson.path("targetRole").asText()).isEqualTo("Backend Engineer");

        String roadmapBody = objectMapper.writeValueAsString(Map.of(
                "profile", Map.of(
                        "resumeText", "Built Java REST APIs using Spring Boot and SQL",
                        "githubLikeText", "Implemented microservices with Docker",
                        "targetRole", "Backend Engineer",
                        "currentSkills", List.of("Git")
                ),
                "durationWeeks", 6,
                "weeklyHours", 8,
                "includePaidResources", false
        ));
        HttpResponse<String> roadmapResp = postJson("/api/roadmap/generate", roadmapBody, token);
        assertThat(roadmapResp.statusCode()).isEqualTo(200);

        String interviewBody = objectMapper.writeValueAsString(Map.of(
                "profile", Map.of(
                        "resumeText", "Built Java APIs with Spring Boot",
                        "githubLikeText", "Used Docker and SQL",
                        "targetRole", "Backend Engineer",
                        "currentSkills", List.of("Git")
                ),
                "newlyAddedSkills", List.of("Docker"),
                "questionCount", 5,
                "preferredDifficulty", "mixed"
        ));
        HttpResponse<String> interviewResp = postJson("/api/interview/questions", interviewBody, token);
        assertThat(interviewResp.statusCode()).isEqualTo(200);
        JsonNode interviewJson = objectMapper.readTree(interviewResp.body());
        JsonNode firstQuestion = interviewJson.path("questions").get(0);
        assertThat(firstQuestion).isNotNull();

        String evalBody = objectMapper.writeValueAsString(Map.of(
                "targetRole", "Backend Engineer",
                "answers", List.of(
                        Map.of(
                                "questionId", firstQuestion.path("questionId").asText(),
                                "question", firstQuestion.path("question").asText(),
                                "expectedFocus", firstQuestion.path("expectedFocus").asText(),
                                "userAnswer", "First I identify bottlenecks, inspect logs, validate SQL indexes, and propose a rollback-safe fix with tests and monitoring.",
                                "skill", firstQuestion.path("skill").asText(),
                                "difficulty", firstQuestion.path("difficulty").asText()
                        )
                )
        ));
        HttpResponse<String> evalResp = postJson("/api/interview/evaluate", evalBody, token);
        assertThat(evalResp.statusCode()).isEqualTo(200);
        JsonNode evalJson = objectMapper.readTree(evalResp.body());
        assertThat(evalJson.path("overallScore").asInt()).isGreaterThanOrEqualTo(0);

        HttpResponse<String> historyResp = getJson("/api/history/me/roadmaps", token);
        assertThat(historyResp.statusCode()).isEqualTo(200);
        assertThat(objectMapper.readTree(historyResp.body()).isArray()).isTrue();

        HttpResponse<String> metricsResp = getJson("/api/metrics/me/summary", token);
        assertThat(metricsResp.statusCode()).isEqualTo(200);
        JsonNode metricsJson = objectMapper.readTree(metricsResp.body());
        assertThat(metricsJson.path("userId").asText()).isEqualTo(email);
        assertThat(metricsJson.path("totalEvents").asInt()).isGreaterThan(0);
    }

    private HttpResponse<String> postJson(String path, String body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null && !token.isBlank()) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getJson(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + path))
                .GET();
        if (token != null && !token.isBlank()) {
            builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
