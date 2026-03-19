package com.PA.BackEnd.dto;

public class interviewAnswerItem {
    private String questionId;
    private String question;
    private String expectedFocus;
    private String userAnswer;
    private String skill;
    private String difficulty;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
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
}
