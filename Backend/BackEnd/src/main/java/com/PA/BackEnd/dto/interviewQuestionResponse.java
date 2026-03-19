package com.PA.BackEnd.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class interviewQuestionResponse {
    private String targetRole;
    private String generationMode;
    private int baselineCoverageScore;
    private List<String> focusSkills = new ArrayList<>();
    private List<interviewQuestionItem> questions = new ArrayList<>();
    private Instant generatedAt = Instant.now();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getGenerationMode() {
        return generationMode;
    }

    public void setGenerationMode(String generationMode) {
        this.generationMode = generationMode;
    }

    public int getBaselineCoverageScore() {
        return baselineCoverageScore;
    }

    public void setBaselineCoverageScore(int baselineCoverageScore) {
        this.baselineCoverageScore = baselineCoverageScore;
    }

    public List<String> getFocusSkills() {
        return focusSkills;
    }

    public void setFocusSkills(List<String> focusSkills) {
        this.focusSkills = focusSkills;
    }

    public List<interviewQuestionItem> getQuestions() {
        return questions;
    }

    public void setQuestions(List<interviewQuestionItem> questions) {
        this.questions = questions;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
