package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.metricsSummaryResponse;
import com.PA.BackEnd.model.analyticsEvent;
import com.PA.BackEnd.repository.analyticsEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class metricsService {
    private final analyticsEventRepository analyticsEventRepository;

    public metricsService(analyticsEventRepository analyticsEventRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
    }

    public void recordAnalyze(String userId, String targetRole, int coverageScore, String analysisMode) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("analysisMode", safe(analysisMode));
        record(userId, "ANALYZE", targetRole, coverageScore, metadata);
    }

    public void recordRoadmap(String userId, String targetRole, int baselineCoverage, int milestoneCount) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("milestoneCount", String.valueOf(milestoneCount));
        record(userId, "ROADMAP", targetRole, baselineCoverage, metadata);
    }

    public void recordInterviewSet(String userId, String targetRole, int questionCount, int baselineCoverage) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("questionCount", String.valueOf(questionCount));
        record(userId, "INTERVIEW_SET", targetRole, baselineCoverage, metadata);
    }

    public void recordInterviewEvaluation(String userId, String targetRole, int overallScore, int answerCount) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("answerCount", String.valueOf(answerCount));
        record(userId, "INTERVIEW_EVAL", targetRole, overallScore, metadata);
    }

    public metricsSummaryResponse getSummaryForUser(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        List<analyticsEvent> events = analyticsEventRepository.findByUserIdOrderByCreatedAtDesc(normalizedUserId);

        metricsSummaryResponse response = new metricsSummaryResponse();
        response.setUserId(normalizedUserId);
        response.setTotalEvents(events.size());
        if (events.isEmpty()) {
            return response;
        }

        int analyzeCount = 0;
        int roadmapCount = 0;
        int interviewSetCount = 0;
        int interviewEvalCount = 0;
        int coverageSum = 0;
        int coverageN = 0;
        int evalSum = 0;
        int evalN = 0;
        Map<String, Integer> eventBreakdown = new LinkedHashMap<>();
        Map<String, Integer> roleCounts = new LinkedHashMap<>();
        Instant latest = events.get(0).getCreatedAt();

        for (analyticsEvent event : events) {
            String eventType = safe(event.getEventType());
            eventBreakdown.put(eventType, eventBreakdown.getOrDefault(eventType, 0) + 1);

            if (!safe(event.getTargetRole()).isBlank()) {
                String role = event.getTargetRole();
                roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);
            }

            switch (eventType) {
                case "ANALYZE" -> {
                    analyzeCount++;
                    coverageSum += event.getNumericValue();
                    coverageN++;
                }
                case "ROADMAP" -> {
                    roadmapCount++;
                    coverageSum += event.getNumericValue();
                    coverageN++;
                }
                case "INTERVIEW_SET" -> interviewSetCount++;
                case "INTERVIEW_EVAL" -> {
                    interviewEvalCount++;
                    evalSum += event.getNumericValue();
                    evalN++;
                }
                default -> {
                    // keep unknown events in breakdown for forward compatibility
                }
            }
        }

        response.setAnalyzeCount(analyzeCount);
        response.setRoadmapCount(roadmapCount);
        response.setInterviewSetCount(interviewSetCount);
        response.setInterviewEvaluationCount(interviewEvalCount);
        response.setAverageCoverageScore(coverageN == 0 ? 0 : (int) Math.round(coverageSum / (double) coverageN));
        response.setAverageInterviewScore(evalN == 0 ? 0 : (int) Math.round(evalSum / (double) evalN));
        response.setTopTargetRole(topRole(roleCounts));
        response.setLastActivityAt(latest);
        response.setEventBreakdown(eventBreakdown);
        return response;
    }

    private void record(String userId, String eventType, String targetRole, int numericValue, Map<String, String> metadata) {
        analyticsEvent event = new analyticsEvent();
        event.setUserId(normalizeUserId(userId));
        event.setEventType(eventType);
        event.setTargetRole(safe(targetRole));
        event.setNumericValue(numericValue);
        event.setMetadata(metadata);
        event.setCreatedAt(Instant.now());
        analyticsEventRepository.save(event);
    }

    private String topRole(Map<String, Integer> roleCounts) {
        String top = "";
        int count = -1;
        for (Map.Entry<String, Integer> entry : roleCounts.entrySet()) {
            if (entry.getValue() > count) {
                top = entry.getKey();
                count = entry.getValue();
            }
        }
        return top;
    }

    private String normalizeUserId(String userId) {
        String normalized = safe(userId).toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "anonymous" : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
