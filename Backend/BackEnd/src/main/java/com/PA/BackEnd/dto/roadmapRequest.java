package com.PA.BackEnd.dto;

public class roadmapRequest {
    private String userId = "anonymous";
    private analyzeRequest profile;
    private int durationWeeks = 8;
    private int weeklyHours = 8;
    private boolean includePaidResources = false;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public analyzeRequest getProfile() {
        return profile;
    }

    public void setProfile(analyzeRequest profile) {
        this.profile = profile;
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

    public boolean isIncludePaidResources() {
        return includePaidResources;
    }

    public void setIncludePaidResources(boolean includePaidResources) {
        this.includePaidResources = includePaidResources;
    }
}
