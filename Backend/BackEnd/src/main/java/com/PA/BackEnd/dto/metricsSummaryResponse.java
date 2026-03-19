package com.PA.BackEnd.dto;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class metricsSummaryResponse {
    private String userId;
    private int totalEvents;
    private int analyzeCount;
    private int roadmapCount;
    private int interviewSetCount;
    private int interviewEvaluationCount;
    private int averageCoverageScore;
    private int averageInterviewScore;
    private String topTargetRole;
    private Instant lastActivityAt;
    private Map<String, Integer> eventBreakdown = new LinkedHashMap<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getAnalyzeCount() {
        return analyzeCount;
    }

    public void setAnalyzeCount(int analyzeCount) {
        this.analyzeCount = analyzeCount;
    }

    public int getRoadmapCount() {
        return roadmapCount;
    }

    public void setRoadmapCount(int roadmapCount) {
        this.roadmapCount = roadmapCount;
    }

    public int getInterviewSetCount() {
        return interviewSetCount;
    }

    public void setInterviewSetCount(int interviewSetCount) {
        this.interviewSetCount = interviewSetCount;
    }

    public int getInterviewEvaluationCount() {
        return interviewEvaluationCount;
    }

    public void setInterviewEvaluationCount(int interviewEvaluationCount) {
        this.interviewEvaluationCount = interviewEvaluationCount;
    }

    public int getAverageCoverageScore() {
        return averageCoverageScore;
    }

    public void setAverageCoverageScore(int averageCoverageScore) {
        this.averageCoverageScore = averageCoverageScore;
    }

    public int getAverageInterviewScore() {
        return averageInterviewScore;
    }

    public void setAverageInterviewScore(int averageInterviewScore) {
        this.averageInterviewScore = averageInterviewScore;
    }

    public String getTopTargetRole() {
        return topTargetRole;
    }

    public void setTopTargetRole(String topTargetRole) {
        this.topTargetRole = topTargetRole;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Map<String, Integer> getEventBreakdown() {
        return eventBreakdown;
    }

    public void setEventBreakdown(Map<String, Integer> eventBreakdown) {
        this.eventBreakdown = eventBreakdown;
    }
}
