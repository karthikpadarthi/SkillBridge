package com.PA.BackEnd.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class interviewEvaluationResponse {
    private String targetRole;
    private int overallScore;
    private String summary;
    private Instant evaluatedAt = Instant.now();
    private List<interviewEvaluationItem> evaluations = new ArrayList<>();
    private List<String> nextActions = new ArrayList<>();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public List<interviewEvaluationItem> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<interviewEvaluationItem> evaluations) {
        this.evaluations = evaluations;
    }

    public List<String> getNextActions() {
        return nextActions;
    }

    public void setNextActions(List<String> nextActions) {
        this.nextActions = nextActions;
    }
}
