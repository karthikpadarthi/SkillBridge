package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.interviewQuestionItem;
import com.PA.BackEnd.dto.interviewQuestionRequest;
import com.PA.BackEnd.dto.interviewQuestionResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class interviewQuestionService {
    private final analysisService analysisService;
    private final historyPersistenceService historyPersistenceService;
    private final interviewQuestionBankService interviewQuestionBankService;

    public interviewQuestionService(analysisService analysisService,
                                    historyPersistenceService historyPersistenceService,
                                    interviewQuestionBankService interviewQuestionBankService) {
        this.analysisService = analysisService;
        this.historyPersistenceService = historyPersistenceService;
        this.interviewQuestionBankService = interviewQuestionBankService;
    }

    public interviewQuestionResponse generateQuestions(interviewQuestionRequest request, String authenticatedUserId) {
        if (request.getProfile() == null) {
            throw new IllegalArgumentException("profile is required");
        }

        gapAnalysisResponse analysis = analysisService.analyze(request.getProfile());
        int questionCount = clamp(request.getQuestionCount(), 3, 20);

        List<String> focusSkills = computeFocusSkills(analysis, request.getNewlyAddedSkills(), questionCount);
        List<interviewQuestionItem> questions = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            String skill = focusSkills.get(i % focusSkills.size());
            String difficulty = difficultyForIndex(request.getPreferredDifficulty(), i, questionCount);
            String type = i % 2 == 0 ? "TECHNICAL" : "SCENARIO";
            questions.add(buildQuestion(i + 1, skill, difficulty, type, analysis.getTargetRole()));
        }

        interviewQuestionResponse response = new interviewQuestionResponse();
        response.setTargetRole(analysis.getTargetRole());
        response.setGenerationMode("RULE_BASED_PIVOT");
        response.setBaselineCoverageScore(analysis.getCoverageScore());
        response.setFocusSkills(focusSkills);
        response.setQuestions(questions);
        response.setGeneratedAt(Instant.now());
        historyPersistenceService.saveInterviewHistory(normalizeUserId(authenticatedUserId), request.getProfile(), response);
        return response;
    }

    private String normalizeUserId(String userId) {
        return (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
    }

    private List<String> computeFocusSkills(gapAnalysisResponse analysis, List<String> newlyAddedSkills, int questionCount) {
        Set<String> ordered = new LinkedHashSet<>();
        if (newlyAddedSkills != null) {
            for (String skill : newlyAddedSkills) {
                if (skill != null && !skill.isBlank()) {
                    ordered.add(skill.trim());
                }
            }
        }

        for (String skill : analysis.getMissingSkills()) {
            ordered.add(skill);
            if (ordered.size() >= questionCount) {
                break;
            }
        }
        for (String skill : analysis.getMatchedSkills()) {
            if (ordered.size() >= questionCount) {
                break;
            }
            ordered.add(skill);
        }

        if (ordered.isEmpty()) {
            ordered.add("Problem Solving");
        }
        return new ArrayList<>(ordered);
    }

    private interviewQuestionItem buildQuestion(int index, String skill, String difficulty, String type, String role) {
        String question = interviewQuestionBankService.getQuestion(role, skill, type, index);
        String expectedFocus = interviewQuestionBankService.getExpectedFocus(role, skill, type);
        String questionId = role.toLowerCase(Locale.ROOT).replace(" ", "-")
                + "-" + skill.toLowerCase(Locale.ROOT).replace(" ", "-")
                + "-" + type.toLowerCase(Locale.ROOT)
                + "-" + index;
        return new interviewQuestionItem(questionId, skill, difficulty, type, question, expectedFocus);
    }

    private String difficultyForIndex(String preferredDifficulty, int index, int total) {
        String normalized = preferredDifficulty == null ? "mixed" : preferredDifficulty.toLowerCase(Locale.ROOT).trim();
        if (normalized.equals("easy") || normalized.equals("medium") || normalized.equals("hard")) {
            return normalized.toUpperCase(Locale.ROOT);
        }

        double ratio = index / (double) total;
        if (ratio < 0.34) {
            return "EASY";
        }
        if (ratio < 0.67) {
            return "MEDIUM";
        }
        return "HARD";
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }
}
