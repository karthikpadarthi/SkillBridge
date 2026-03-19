package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;

public class analyzeRequest {
    private String resumeText;
    private String githubLikeText;
    private String targetRole;
    private List<String> currentSkills = new ArrayList<>();

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getGithubLikeText() {
        return githubLikeText;
    }

    public void setGithubLikeText(String githubLikeText) {
        this.githubLikeText = githubLikeText;
    }

    public List<String> getCurrentSkills() {
        return currentSkills;
    }

    public void setCurrentSkills(List<String> currentSkills) {
        this.currentSkills = currentSkills;
    }
}
