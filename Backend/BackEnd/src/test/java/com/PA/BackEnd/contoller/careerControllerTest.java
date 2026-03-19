package com.PA.BackEnd.contoller;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.metricsSummaryResponse;
import com.PA.BackEnd.dto.roadmapRequest;
import com.PA.BackEnd.dto.roadmapResponse;
import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.model.roadmapPlan;
import com.PA.BackEnd.repository.interviewQuestionSetRepository;
import com.PA.BackEnd.repository.jobRepository;
import com.PA.BackEnd.repository.roadmapPlanRepository;
import com.PA.BackEnd.repository.userProfileSnapshotRepository;
import com.PA.BackEnd.service.analysisService;
import com.PA.BackEnd.service.interviewEvaluationService;
import com.PA.BackEnd.service.interviewQuestionService;
import com.PA.BackEnd.service.jobIngestionService;
import com.PA.BackEnd.service.metricsService;
import com.PA.BackEnd.service.pdfTextExtractionService;
import com.PA.BackEnd.service.roadmapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class careerControllerTest {

    @Mock
    private analysisService analysisService;
    @Mock
    private jobRepository jobRepository;
    @Mock
    private jobIngestionService jobIngestionService;
    @Mock
    private roadmapService roadmapService;
    @Mock
    private interviewQuestionService interviewQuestionService;
    @Mock
    private interviewEvaluationService interviewEvaluationService;
    @Mock
    private metricsService metricsService;
    @Mock
    private pdfTextExtractionService pdfTextExtractionService;
    @Mock
    private roadmapPlanRepository roadmapPlanRepository;
    @Mock
    private interviewQuestionSetRepository interviewQuestionSetRepository;
    @Mock
    private userProfileSnapshotRepository userProfileSnapshotRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private careerController careerController;

    @Test
    void analyze_shouldDelegateAndRecordMetrics() {
        analyzeRequest request = new analyzeRequest();
        request.setTargetRole("Backend Engineer");

        gapAnalysisResponse analysis = new gapAnalysisResponse();
        analysis.setTargetRole("Backend Engineer");
        analysis.setCoverageScore(67);
        analysis.setAnalysisMode("FALLBACK_RULE_BASED");

        when(authentication.getName()).thenReturn("alice@example.com");
        when(analysisService.analyze(request)).thenReturn(analysis);

        gapAnalysisResponse result = careerController.analyze(request, authentication);

        assertThat(result.getTargetRole()).isEqualTo("Backend Engineer");
        verify(metricsService).recordAnalyze("alice@example.com", "Backend Engineer", 67, "FALLBACK_RULE_BASED");
    }

    @Test
    void getRoles_shouldReturnRepositoryValues() {
        List<jobRole> roles = List.of(new jobRole("Backend Engineer", List.of("Java")));
        when(jobRepository.findAll()).thenReturn(roles);

        List<jobRole> result = careerController.getRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleName()).isEqualTo("Backend Engineer");
    }

    @Test
    void getRoadmapHistory_shouldScopeToAuthenticatedUser() {
        roadmapPlan plan = new roadmapPlan();
        plan.setTargetRole("Backend Engineer");

        when(authentication.getName()).thenReturn("alice@example.com");
        when(roadmapPlanRepository.findByUserIdOrderByGeneratedAtDesc("alice@example.com"))
                .thenReturn(List.of(plan));

        List<roadmapPlan> result = careerController.getRoadmapHistory(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetRole()).isEqualTo("Backend Engineer");
    }

    @Test
    void getMetricsSummary_shouldDelegateWithAuthenticatedUser() {
        metricsSummaryResponse summary = new metricsSummaryResponse();
        summary.setUserId("alice@example.com");
        summary.setTotalEvents(5);
        when(authentication.getName()).thenReturn("alice@example.com");
        when(metricsService.getSummaryForUser("alice@example.com")).thenReturn(summary);

        metricsSummaryResponse result = careerController.getMetricsSummary(authentication);

        assertThat(result.getUserId()).isEqualTo("alice@example.com");
        assertThat(result.getTotalEvents()).isEqualTo(5);
    }

    @Test
    void analyzeResumePdf_shouldExtractTextAnalyzeAndRecordMetrics() {
        MockMultipartFile file = new MockMultipartFile(
                "resumeFile",
                "resume.pdf",
                "application/pdf",
                "dummy".getBytes()
        );
        when(authentication.getName()).thenReturn("alice@example.com");
        when(pdfTextExtractionService.extractText(file)).thenReturn("Java Spring Boot SQL");

        gapAnalysisResponse analysis = new gapAnalysisResponse();
        analysis.setTargetRole("Backend Engineer");
        analysis.setCoverageScore(70);
        analysis.setAnalysisMode("AI_ASSISTED");
        when(analysisService.analyze(any(analyzeRequest.class))).thenReturn(analysis);

        gapAnalysisResponse result = careerController.analyzeResumePdf(
                file,
                "Backend Engineer",
                "GitHub sample",
                List.of("Git"),
                authentication
        );

        assertThat(result.getTargetRole()).isEqualTo("Backend Engineer");
        ArgumentCaptor<analyzeRequest> captor = ArgumentCaptor.forClass(analyzeRequest.class);
        verify(analysisService).analyze(captor.capture());
        assertThat(captor.getValue().getResumeText()).isEqualTo("Java Spring Boot SQL");
        assertThat(captor.getValue().getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(captor.getValue().getCurrentSkills()).containsExactly("Git");
        verify(metricsService).recordAnalyze("alice@example.com", "Backend Engineer", 70, "AI_ASSISTED");
    }

    @Test
    void generateRoadmapFromResumePdf_shouldBuildProfileAndRecordMetrics() {
        MockMultipartFile file = new MockMultipartFile(
                "resumeFile",
                "resume.pdf",
                "application/pdf",
                "dummy".getBytes()
        );
        when(authentication.getName()).thenReturn("alice@example.com");
        when(pdfTextExtractionService.extractText(file)).thenReturn("Java Spring Boot SQL");

        roadmapResponse roadmap = new roadmapResponse();
        roadmap.setTargetRole("Backend Engineer");
        roadmap.setBaselineCoverageScore(65);
        roadmap.setMilestones(List.of());
        when(roadmapService.generateRoadmap(any(roadmapRequest.class), eq("alice@example.com"))).thenReturn(roadmap);

        roadmapResponse result = careerController.generateRoadmapFromResumePdf(
                file,
                "Backend Engineer",
                "GitHub sample",
                List.of("Git"),
                6,
                8,
                false,
                authentication
        );

        assertThat(result.getTargetRole()).isEqualTo("Backend Engineer");
        ArgumentCaptor<roadmapRequest> captor = ArgumentCaptor.forClass(roadmapRequest.class);
        verify(roadmapService).generateRoadmap(captor.capture(), eq("alice@example.com"));
        assertThat(captor.getValue().getProfile().getResumeText()).isEqualTo("Java Spring Boot SQL");
        assertThat(captor.getValue().getProfile().getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(captor.getValue().getDurationWeeks()).isEqualTo(6);
        assertThat(captor.getValue().getWeeklyHours()).isEqualTo(8);
        verify(metricsService).recordRoadmap("alice@example.com", "Backend Engineer", 65, 0);
    }
}
