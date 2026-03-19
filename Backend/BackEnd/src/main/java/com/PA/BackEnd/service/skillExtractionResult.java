package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.skillEvidence;

import java.util.ArrayList;
import java.util.List;

public class skillExtractionResult {
    private List<String> skills = new ArrayList<>();
    private List<skillEvidence> evidences = new ArrayList<>();
    private String mode;
    private String fallbackReason;

    public skillExtractionResult() {}

    public skillExtractionResult(List<String> skills, String mode, String fallbackReason) {
        this.skills = skills;
        this.mode = mode;
        this.fallbackReason = fallbackReason;
    }

    public skillExtractionResult(List<String> skills, List<skillEvidence> evidences, String mode, String fallbackReason) {
        this.skills = skills;
        this.evidences = evidences;
        this.mode = mode;
        this.fallbackReason = fallbackReason;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<skillEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<skillEvidence> evidences) {
        this.evidences = evidences;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }

    public void setFallbackReason(String fallbackReason) {
        this.fallbackReason = fallbackReason;
    }
}
