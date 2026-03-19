package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;

public class interviewQuestionRequest {
    private String userId = "anonymous";
    private analyzeRequest profile;
    private List<String> newlyAddedSkills = new ArrayList<>();
    private int questionCount = 8;
    private String preferredDifficulty = "mixed";

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

    public List<String> getNewlyAddedSkills() {
        return newlyAddedSkills;
    }

    public void setNewlyAddedSkills(List<String> newlyAddedSkills) {
        this.newlyAddedSkills = newlyAddedSkills;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public String getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(String preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }
}
