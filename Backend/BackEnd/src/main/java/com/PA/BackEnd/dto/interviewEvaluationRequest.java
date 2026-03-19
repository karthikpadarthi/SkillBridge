package com.PA.BackEnd.dto;

import java.util.ArrayList;
import java.util.List;

public class interviewEvaluationRequest {
    private String targetRole;
    private List<interviewAnswerItem> answers = new ArrayList<>();

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public List<interviewAnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<interviewAnswerItem> answers) {
        this.answers = answers;
    }
}
