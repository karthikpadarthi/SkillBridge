package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.skillEvidence;
import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.repository.jobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class analysisServiceTest {

    @Mock
    private jobRepository jobRepository;

    @Mock
    private aiSkillExtractor aiSkillExtractor;

    @Mock
    private ruleBasedSkillExtractor ruleBasedSkillExtractor;

    @InjectMocks
    private analysisService analysisService;

    @Test
    void analyze_whenAiFails_shouldFallbackAndNormalizeEvidence() {
        analyzeRequest request = new analyzeRequest();
        request.setTargetRole("Backend Engineer");
        request.setResumeText("Built Java APIs and Dockerized services");
        request.setGithubLikeText("Spring Boot + CI/CD");
        request.setCurrentSkills(List.of("Git", "Communication"));

        jobRole role = new jobRole();
        role.setRoleName("Backend Engineer");
        role.setRequiredSkills(List.of("Java", "Spring Boot", "Docker"));
        role.setSkillDemand(Map.of("Spring Boot", 90, "Docker", 85, "Java", 80));
        when(jobRepository.findByRoleNameIgnoreCase("Backend Engineer")).thenReturn(Optional.of(role));

        when(aiSkillExtractor.extractSkills(request.getResumeText(), request.getGithubLikeText()))
                .thenThrow(new RuntimeException("timeout"));

        skillExtractionResult fallback = new skillExtractionResult(
                Arrays.asList("Java", "Git", "Java", "", null),
                List.of(
                        new skillEvidence("Java", " ", 1.2),
                        new skillEvidence("Git", "RULE_BASED", -0.5),
                        new skillEvidence("Unknown", "RULE_BASED", 0.8)
                ),
                "FALLBACK_RULE_BASED",
                null
        );
        when(ruleBasedSkillExtractor.extractSkills(request.getResumeText(), request.getGithubLikeText()))
                .thenReturn(fallback);

        gapAnalysisResponse response = analysisService.analyze(request);

        assertThat(response.getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(response.getAnalysisMode()).isEqualTo("FALLBACK_RULE_BASED");
        assertThat(response.getFallbackReason()).startsWith("AI unavailable:");
        assertThat(response.getExtractedSkills()).containsExactly("Java", "Git");
        assertThat(response.getMatchedSkills()).containsExactly("Java");
        assertThat(response.getMissingSkills()).containsExactly("Spring Boot", "Docker");
        assertThat(response.getCoverageScore()).isEqualTo(33);
        assertThat(response.getTransferableSkills()).containsExactly("Git", "Communication");
        assertThat(response.getExtractedSkillEvidence()).hasSize(2);
        assertThat(response.getExtractedSkillEvidence().get(0).getSource()).isEqualTo("UNKNOWN");
        assertThat(response.getExtractedSkillEvidence().get(0).getConfidence()).isEqualTo(1.0);
        assertThat(response.getExtractedSkillEvidence().get(1).getConfidence()).isEqualTo(0.0);
        assertThat(response.getNextStep()).contains("Spring Boot");

        verify(aiSkillExtractor).extractSkills(request.getResumeText(), request.getGithubLikeText());
        verify(ruleBasedSkillExtractor).extractSkills(request.getResumeText(), request.getGithubLikeText());
    }
}
