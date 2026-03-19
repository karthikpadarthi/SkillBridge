package com.PA.BackEnd.model;

import com.PA.BackEnd.dto.interviewQuestionItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "interview_question_sets")
public class interviewQuestionSet {
    @Id
    private String id;
    private String userId;
    private String profileSnapshotId;
    private String targetRole;
    private String generationMode;
    private int baselineCoverageScore;
    private List<String> focusSkills = new ArrayList<>();
    private List<interviewQuestionItem> questions = new ArrayList<>();
    private Instant generatedAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileSnapshotId() {
        return profileSnapshotId;
    }

    public void setProfileSnapshotId(String profileSnapshotId) {
        this.profileSnapshotId = profileSnapshotId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getGenerationMode() {
        return generationMode;
    }

    public void setGenerationMode(String generationMode) {
        this.generationMode = generationMode;
    }

    public int getBaselineCoverageScore() {
        return baselineCoverageScore;
    }

    public void setBaselineCoverageScore(int baselineCoverageScore) {
        this.baselineCoverageScore = baselineCoverageScore;
    }

    public List<String> getFocusSkills() {
        return focusSkills;
    }

    public void setFocusSkills(List<String> focusSkills) {
        this.focusSkills = focusSkills;
    }

    public List<interviewQuestionItem> getQuestions() {
        return questions;
    }

    public void setQuestions(List<interviewQuestionItem> questions) {
        this.questions = questions;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
