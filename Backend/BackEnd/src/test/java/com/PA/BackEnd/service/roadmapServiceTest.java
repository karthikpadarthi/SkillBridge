package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.learningResourceItem;
import com.PA.BackEnd.dto.roadmapRequest;
import com.PA.BackEnd.dto.roadmapResponse;
import com.PA.BackEnd.dto.skillPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class roadmapServiceTest {

    @Mock
    private analysisService analysisService;

    @Mock
    private learningResourceCatalogService catalogService;

    @Mock
    private historyPersistenceService historyPersistenceService;

    @InjectMocks
    private roadmapService roadmapService;

    @Test
    void generateRoadmap_shouldClampInputsBuildMilestonesAndPersistHistory() {
        analyzeRequest profile = new analyzeRequest();
        profile.setTargetRole("Backend Engineer");

        roadmapRequest request = new roadmapRequest();
        request.setProfile(profile);
        request.setDurationWeeks(1);
        request.setWeeklyHours(1);
        request.setIncludePaidResources(false);

        gapAnalysisResponse analysis = new gapAnalysisResponse();
        analysis.setTargetRole("Backend Engineer");
        analysis.setCoverageScore(40);
        analysis.setPrioritizedMissingSkills(List.of(
                new skillPriority("Spring Boot", 90, "HIGH"),
                new skillPriority("Docker", 85, "MEDIUM")
        ));
        when(analysisService.analyze(profile)).thenReturn(analysis);
        when(catalogService.getResources(eq("Spring Boot"), eq(false)))
                .thenReturn(List.of(new learningResourceItem("Spring", "Spring Docs", "https://spring.io", true, 4)));
        when(catalogService.getResources(eq("Docker"), eq(false)))
                .thenReturn(List.of(new learningResourceItem("Docker", "Docker Docs", "https://docs.docker.com", true, 3)));

        roadmapResponse response = roadmapService.generateRoadmap(request, "   ");

        assertThat(response.getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(response.getDurationWeeks()).isEqualTo(2);
        assertThat(response.getWeeklyHours()).isEqualTo(2);
        assertThat(response.getMilestones()).hasSize(2);
        assertThat(response.getMilestones().get(0).getSkill()).isEqualTo("Spring Boot");
        assertThat(response.getMilestones().get(1).getSkill()).isEqualTo("Docker");
        assertThat(response.getSummary()).contains("2 planned milestones");
        verify(historyPersistenceService).saveRoadmapHistory(eq("anonymous"), eq(profile), eq(response));
    }

    @Test
    void generateRoadmap_whenNoMissingSkills_shouldReturnInterviewPrepPlan() {
        analyzeRequest profile = new analyzeRequest();
        profile.setTargetRole("Backend Engineer");

        roadmapRequest request = new roadmapRequest();
        request.setProfile(profile);
        request.setDurationWeeks(6);
        request.setWeeklyHours(8);

        gapAnalysisResponse analysis = new gapAnalysisResponse();
        analysis.setTargetRole("Backend Engineer");
        analysis.setCoverageScore(100);
        analysis.setPrioritizedMissingSkills(List.of());
        when(analysisService.analyze(profile)).thenReturn(analysis);

        roadmapResponse response = roadmapService.generateRoadmap(request, "user@test.com");

        assertThat(response.getMilestones()).hasSize(1);
        assertThat(response.getMilestones().get(0).getSkill()).isEqualTo("Interview Preparation");
        assertThat(response.getSummary()).isEqualTo("Role baseline is already met. Focus on interview readiness.");
        verify(historyPersistenceService).saveRoadmapHistory(eq("user@test.com"), eq(profile), eq(response));
    }
}

