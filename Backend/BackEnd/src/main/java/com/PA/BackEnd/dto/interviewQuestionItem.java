package com.PA.BackEnd.dto;

public class interviewQuestionItem {
    private String questionId;
    private String skill;
    private String difficulty;
    private String type;
    private String question;
    private String expectedFocus;

    public interviewQuestionItem() {}

    public interviewQuestionItem(String questionId,
                                 String skill,
                                 String difficulty,
                                 String type,
                                 String question,
                                 String expectedFocus) {
        this.questionId = questionId;
        this.skill = skill;
        this.difficulty = difficulty;
        this.type = type;
        this.question = question;
        this.expectedFocus = expectedFocus;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExpectedFocus() {
        return expectedFocus;
    }

    public void setExpectedFocus(String expectedFocus) {
        this.expectedFocus = expectedFocus;
    }
}
