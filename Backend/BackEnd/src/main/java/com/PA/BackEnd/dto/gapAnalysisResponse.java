package com.PA.BackEnd.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class gapAnalysisResponse {
    private String targetRole;
    private String analysisMode;
    private String fallbackReason;
    private List<String> extractedSkills = new ArrayList<>();
    private List<skillEvidence> extractedSkillEvidence = new ArrayList<>();
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private List<String> transferableSkills = new ArrayList<>();
    private List<skillPriority> prioritizedMissingSkills = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();
    private int coverageScore;
    private String nextStep;
    private Instant generatedAt = Instant.now();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(String analysisMode) {
        this.analysisMode = analysisMode;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }

    public void setFallbackReason(String fallbackReason) {
        this.fallbackReason = fallbackReason;
    }

    public List<String> getExtractedSkills() {
        return extractedSkills;
    }

    public void setExtractedSkills(List<String> extractedSkills) {
        this.extractedSkills = extractedSkills;
    }

    public List<skillEvidence> getExtractedSkillEvidence() {
        return extractedSkillEvidence;
    }

    public void setExtractedSkillEvidence(List<skillEvidence> extractedSkillEvidence) {
        this.extractedSkillEvidence = extractedSkillEvidence;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getTransferableSkills() {
        return transferableSkills;
    }

    public void setTransferableSkills(List<String> transferableSkills) {
        this.transferableSkills = transferableSkills;
    }

    public List<skillPriority> getPrioritizedMissingSkills() {
        return prioritizedMissingSkills;
    }

    public void setPrioritizedMissingSkills(List<skillPriority> prioritizedMissingSkills) {
        this.prioritizedMissingSkills = prioritizedMissingSkills;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public int getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(int coverageScore) {
        this.coverageScore = coverageScore;
    }

    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
