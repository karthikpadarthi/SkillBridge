package com.PA.BackEnd.dto;

public class skillEvidence {
    private String skill;
    private String source;
    private double confidence;

    public skillEvidence() {}

    public skillEvidence(String skill, String source, double confidence) {
        this.skill = skill;
        this.source = source;
        this.confidence = confidence;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
