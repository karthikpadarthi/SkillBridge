package com.PA.BackEnd.dto;

public class skillPriority {
    private String skill;
    private int marketDemandScore;
    private String urgency;

    public skillPriority() {}

    public skillPriority(String skill, int marketDemandScore, String urgency) {
        this.skill = skill;
        this.marketDemandScore = marketDemandScore;
        this.urgency = urgency;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public int getMarketDemandScore() {
        return marketDemandScore;
    }

    public void setMarketDemandScore(int marketDemandScore) {
        this.marketDemandScore = marketDemandScore;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
}
