package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.interviewQuestionResponse;
import com.PA.BackEnd.dto.roadmapResponse;
import com.PA.BackEnd.model.interviewQuestionSet;
import com.PA.BackEnd.model.roadmapPlan;
import com.PA.BackEnd.model.userProfileSnapshot;
import com.PA.BackEnd.repository.interviewQuestionSetRepository;
import com.PA.BackEnd.repository.roadmapPlanRepository;
import com.PA.BackEnd.repository.userProfileSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class historyPersistenceService {
    private final userProfileSnapshotRepository profileSnapshotRepository;
    private final roadmapPlanRepository roadmapPlanRepository;
    private final interviewQuestionSetRepository interviewQuestionSetRepository;

    public historyPersistenceService(userProfileSnapshotRepository profileSnapshotRepository,
                                     roadmapPlanRepository roadmapPlanRepository,
                                     interviewQuestionSetRepository interviewQuestionSetRepository) {
        this.profileSnapshotRepository = profileSnapshotRepository;
        this.roadmapPlanRepository = roadmapPlanRepository;
        this.interviewQuestionSetRepository = interviewQuestionSetRepository;
    }

    public roadmapPlan saveRoadmapHistory(String userId, analyzeRequest profile, roadmapResponse response) {
        userProfileSnapshot snapshot = saveProfileSnapshot(userId, profile);

        roadmapPlan plan = new roadmapPlan();
        plan.setUserId(userId);
        plan.setProfileSnapshotId(snapshot.getId());
        plan.setTargetRole(response.getTargetRole());
        plan.setDurationWeeks(response.getDurationWeeks());
        plan.setWeeklyHours(response.getWeeklyHours());
        plan.setBaselineCoverageScore(response.getBaselineCoverageScore());
        plan.setSummary(response.getSummary());
        plan.setMilestones(response.getMilestones());
        plan.setGeneratedAt(response.getGeneratedAt() == null ? Instant.now() : response.getGeneratedAt());
        return roadmapPlanRepository.save(plan);
    }

    public interviewQuestionSet saveInterviewHistory(String userId, analyzeRequest profile, interviewQuestionResponse response) {
        userProfileSnapshot snapshot = saveProfileSnapshot(userId, profile);

        interviewQuestionSet set = new interviewQuestionSet();
        set.setUserId(userId);
        set.setProfileSnapshotId(snapshot.getId());
        set.setTargetRole(response.getTargetRole());
        set.setGenerationMode(response.getGenerationMode());
        set.setBaselineCoverageScore(response.getBaselineCoverageScore());
        set.setFocusSkills(response.getFocusSkills());
        set.setQuestions(response.getQuestions());
        set.setGeneratedAt(response.getGeneratedAt() == null ? Instant.now() : response.getGeneratedAt());
        return interviewQuestionSetRepository.save(set);
    }

    private userProfileSnapshot saveProfileSnapshot(String userId, analyzeRequest profile) {
        userProfileSnapshot snapshot = new userProfileSnapshot();
        snapshot.setUserId(userId);
        snapshot.setResumeText(profile.getResumeText());
        snapshot.setGithubLikeText(profile.getGithubLikeText());
        snapshot.setTargetRole(profile.getTargetRole());
        snapshot.setCurrentSkills(profile.getCurrentSkills());
        snapshot.setCreatedAt(Instant.now());
        return profileSnapshotRepository.save(snapshot);
    }
}
