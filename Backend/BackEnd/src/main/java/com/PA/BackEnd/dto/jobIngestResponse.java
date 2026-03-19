package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class jobIngestResponse {
    private String targetRole;
    private int ingestedCount;
    private int duplicatesSkipped;
    private int totalPostingsForRole;
    private int averageReliabilityScore;
    private List<String> topSkills = new ArrayList<>();
    private Map<String, Integer> aggregatedDemand;
    private Map<String, Integer> sourceQualityBreakdown;

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getIngestedCount() {
        return ingestedCount;
    }

    public void setIngestedCount(int ingestedCount) {
        this.ingestedCount = ingestedCount;
    }

    public int getDuplicatesSkipped() {
        return duplicatesSkipped;
    }

    public void setDuplicatesSkipped(int duplicatesSkipped) {
        this.duplicatesSkipped = duplicatesSkipped;
    }

    public int getTotalPostingsForRole() {
        return totalPostingsForRole;
    }

    public void setTotalPostingsForRole(int totalPostingsForRole) {
        this.totalPostingsForRole = totalPostingsForRole;
    }

    public int getAverageReliabilityScore() {
        return averageReliabilityScore;
    }

    public void setAverageReliabilityScore(int averageReliabilityScore) {
        this.averageReliabilityScore = averageReliabilityScore;
    }

    public List<String> getTopSkills() {
        return topSkills;
    }

    public void setTopSkills(List<String> topSkills) {
        this.topSkills = topSkills;
    }

    public Map<String, Integer> getAggregatedDemand() {
        return aggregatedDemand;
    }

    public void setAggregatedDemand(Map<String, Integer> aggregatedDemand) {
        this.aggregatedDemand = aggregatedDemand;
    }

    public Map<String, Integer> getSourceQualityBreakdown() {
        return sourceQualityBreakdown;
    }

    public void setSourceQualityBreakdown(Map<String, Integer> sourceQualityBreakdown) {
        this.sourceQualityBreakdown = sourceQualityBreakdown;
    }
}
