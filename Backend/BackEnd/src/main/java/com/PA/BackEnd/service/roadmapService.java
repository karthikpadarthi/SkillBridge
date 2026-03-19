package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.learningResourceItem;
import com.PA.BackEnd.dto.roadmapMilestone;
import com.PA.BackEnd.dto.roadmapRequest;
import com.PA.BackEnd.dto.roadmapResponse;
import com.PA.BackEnd.dto.skillPriority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class roadmapService {
    private final analysisService analysisService;
    private final learningResourceCatalogService catalogService;
    private final historyPersistenceService historyPersistenceService;

    public roadmapService(analysisService analysisService,
                          learningResourceCatalogService catalogService,
                          historyPersistenceService historyPersistenceService) {
        this.analysisService = analysisService;
        this.catalogService = catalogService;
        this.historyPersistenceService = historyPersistenceService;
    }

    public roadmapResponse generateRoadmap(roadmapRequest request, String authenticatedUserId) {
        if (request.getProfile() == null) {
            throw new IllegalArgumentException("profile is required");
        }

        int durationWeeks = clamp(request.getDurationWeeks(), 2, 24);
        int weeklyHours = clamp(request.getWeeklyHours(), 2, 30);

        gapAnalysisResponse analysis = analysisService.analyze(request.getProfile());
        List<skillPriority> prioritized = analysis.getPrioritizedMissingSkills();
        roadmapResponse response;
        if (prioritized == null || prioritized.isEmpty()) {
            response = completedReadyRoadmap(analysis, durationWeeks, weeklyHours);
        } else {
            List<roadmapMilestone> milestones = new ArrayList<>();
            int week = 1;
            for (skillPriority priority : prioritized) {
                if (week > durationWeeks) {
                    break;
                }
                roadmapMilestone milestone = buildMilestone(week, weeklyHours, priority, request.isIncludePaidResources());
                milestones.add(milestone);
                week++;
            }

            while (week <= durationWeeks) {
                roadmapMilestone revisionWeek = new roadmapMilestone();
                revisionWeek.setWeek(week);
                revisionWeek.setSkill("Revision + Mock Interview");
                revisionWeek.setObjective("Consolidate learned skills and practice interview scenarios.");
                revisionWeek.setResources(List.of(
                        new learningResourceItem("Mock Interview Practice", "Pramp", "https://www.pramp.com/", true, Math.max(2, weeklyHours / 2))
                ));
                revisionWeek.setHandsOnProject("Refactor one previous project and add measurable improvements.");
                revisionWeek.setCheckpoint("Record one mock interview and write a retrospective.");
                milestones.add(revisionWeek);
                week++;
            }

            response = new roadmapResponse();
            response.setTargetRole(analysis.getTargetRole());
            response.setDurationWeeks(durationWeeks);
            response.setWeeklyHours(weeklyHours);
            response.setBaselineCoverageScore(analysis.getCoverageScore());
            response.setSummary(buildSummary(analysis, milestones.size()));
            response.setGeneratedAt(Instant.now());
            response.setMilestones(milestones);
        }
        historyPersistenceService.saveRoadmapHistory(normalizeUserId(authenticatedUserId), request.getProfile(), response);
        return response;
    }

    private roadmapMilestone buildMilestone(int week,
                                            int weeklyHours,
                                            skillPriority priority,
                                            boolean includePaidResources) {
        roadmapMilestone milestone = new roadmapMilestone();
        milestone.setWeek(week);
        milestone.setSkill(priority.getSkill());
        milestone.setObjective("Build practical competency in " + priority.getSkill()
                + " with focus on " + priority.getUrgency() + " priority outcomes.");
        milestone.setResources(catalogService.getResources(priority.getSkill(), includePaidResources));
        milestone.setHandsOnProject(projectForSkill(priority.getSkill(), weeklyHours));
        milestone.setCheckpoint(checkpointForSkill(priority.getSkill()));
        return milestone;
    }

    private roadmapResponse completedReadyRoadmap(gapAnalysisResponse analysis, int durationWeeks, int weeklyHours) {
        roadmapMilestone milestone = new roadmapMilestone();
        milestone.setWeek(1);
        milestone.setSkill("Interview Preparation");
        milestone.setObjective("Your profile already matches the role baseline. Focus on interviews and portfolio.");
        milestone.setResources(List.of(
                new learningResourceItem("Technical Interview Handbook", "Tech Interview Handbook", "https://www.techinterviewhandbook.org/", true, 6)
        ));
        milestone.setHandsOnProject("Polish one portfolio project and prepare architecture walkthrough.");
        milestone.setCheckpoint("Complete two mock interviews and refine STAR stories.");

        roadmapResponse response = new roadmapResponse();
        response.setTargetRole(analysis.getTargetRole());
        response.setDurationWeeks(durationWeeks);
        response.setWeeklyHours(weeklyHours);
        response.setBaselineCoverageScore(analysis.getCoverageScore());
        response.setSummary("Role baseline is already met. Focus on interview readiness.");
        response.setGeneratedAt(Instant.now());
        response.setMilestones(List.of(milestone));
        return response;
    }

    private String normalizeUserId(String userId) {
        return (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private String buildSummary(gapAnalysisResponse analysis, int milestoneCount) {
        return "Roadmap created for " + analysis.getTargetRole()
                + " with baseline coverage " + analysis.getCoverageScore()
                + "% across " + milestoneCount + " planned milestones.";
    }

    private String projectForSkill(String skill, int weeklyHours) {
        return "Build a mini project showcasing " + skill
                + " and spend ~" + Math.max(3, weeklyHours / 2)
                + " hours on implementation/testing.";
    }

    private String checkpointForSkill(String skill) {
        return "Publish project notes and demonstrate one deliverable proving " + skill + " proficiency.";
    }
}
