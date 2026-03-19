package com.PA.BackEnd.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class roadmapResponse {
    private String targetRole;
    private int durationWeeks;
    private int weeklyHours;
    private int baselineCoverageScore;
    private String summary;
    private Instant generatedAt = Instant.now();
    private List<roadmapMilestone> milestones = new ArrayList<>();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public int getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(int weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public int getBaselineCoverageScore() {
        return baselineCoverageScore;
    }

    public void setBaselineCoverageScore(int baselineCoverageScore) {
        this.baselineCoverageScore = baselineCoverageScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<roadmapMilestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<roadmapMilestone> milestones) {
        this.milestones = milestones;
    }
}
