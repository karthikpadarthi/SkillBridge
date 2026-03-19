package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;

public class roadmapMilestone {
    private int week;
    private String skill;
    private String objective;
    private List<learningResourceItem> resources = new ArrayList<>();
    private String handsOnProject;
    private String checkpoint;

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public List<learningResourceItem> getResources() {
        return resources;
    }

    public void setResources(List<learningResourceItem> resources) {
        this.resources = resources;
    }

    public String getHandsOnProject() {
        return handsOnProject;
    }

    public void setHandsOnProject(String handsOnProject) {
        this.handsOnProject = handsOnProject;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }
}
