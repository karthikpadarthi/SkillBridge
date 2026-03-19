package com.PA.BackEnd.model;

import com.PA.BackEnd.dto.roadmapMilestone;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "roadmap_plans")
public class roadmapPlan {
    @Id
    private String id;
    private String userId;
    private String profileSnapshotId;
    private String targetRole;
    private int durationWeeks;
    private int weeklyHours;
    private int baselineCoverageScore;
    private String summary;
    private Instant generatedAt = Instant.now();
    private List<roadmapMilestone> milestones = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileSnapshotId() {
        return profileSnapshotId;
    }

    public void setProfileSnapshotId(String profileSnapshotId) {
        this.profileSnapshotId = profileSnapshotId;
    }

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
