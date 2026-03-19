package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.metricsSummaryResponse;
import com.PA.BackEnd.model.analyticsEvent;
import com.PA.BackEnd.repository.analyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class metricsServiceTest {

    @Mock
    private analyticsEventRepository analyticsEventRepository;

    @InjectMocks
    private metricsService metricsService;

    @Test
    void recordAnalyze_shouldPersistNormalizedEvent() {
        metricsService.recordAnalyze(" User@Test.com ", "Backend Engineer", 72, " AI ");

        ArgumentCaptor<analyticsEvent> captor = ArgumentCaptor.forClass(analyticsEvent.class);
        verify(analyticsEventRepository).save(captor.capture());
        analyticsEvent saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo("user@test.com");
        assertThat(saved.getEventType()).isEqualTo("ANALYZE");
        assertThat(saved.getTargetRole()).isEqualTo("Backend Engineer");
        assertThat(saved.getNumericValue()).isEqualTo(72);
        assertThat(saved.getMetadata()).containsEntry("analysisMode", "AI");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void getSummaryForUser_shouldAggregateCountsAveragesAndBreakdown() {
        Instant now = Instant.parse("2026-03-19T06:00:00Z");
        List<analyticsEvent> events = List.of(
                event("user@test.com", "ANALYZE", "Backend Engineer", 60, now),
                event("user@test.com", "ROADMAP", "Backend Engineer", 80, now.minusSeconds(60)),
                event("user@test.com", "INTERVIEW_SET", "Backend Engineer", 0, now.minusSeconds(120)),
                event("user@test.com", "INTERVIEW_EVAL", "Backend Engineer", 70, now.minusSeconds(180)),
                event("user@test.com", "CUSTOM_EVENT", "Cloud Engineer", 10, now.minusSeconds(240))
        );
        when(analyticsEventRepository.findByUserIdOrderByCreatedAtDesc(eq("user@test.com"))).thenReturn(events);

        metricsSummaryResponse response = metricsService.getSummaryForUser(" User@Test.com ");

        assertThat(response.getUserId()).isEqualTo("user@test.com");
        assertThat(response.getTotalEvents()).isEqualTo(5);
        assertThat(response.getAnalyzeCount()).isEqualTo(1);
        assertThat(response.getRoadmapCount()).isEqualTo(1);
        assertThat(response.getInterviewSetCount()).isEqualTo(1);
        assertThat(response.getInterviewEvaluationCount()).isEqualTo(1);
        assertThat(response.getAverageCoverageScore()).isEqualTo(70);
        assertThat(response.getAverageInterviewScore()).isEqualTo(70);
        assertThat(response.getTopTargetRole()).isEqualTo("Backend Engineer");
        assertThat(response.getLastActivityAt()).isEqualTo(now);
        assertThat(response.getEventBreakdown()).containsEntry("CUSTOM_EVENT", 1);
    }

    private analyticsEvent event(String userId, String type, String role, int value, Instant createdAt) {
        analyticsEvent event = new analyticsEvent();
        event.setUserId(userId);
        event.setEventType(type);
        event.setTargetRole(role);
        event.setNumericValue(value);
        event.setCreatedAt(createdAt);
        return event;
    }
}

