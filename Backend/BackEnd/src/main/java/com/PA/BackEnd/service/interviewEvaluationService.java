package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.interviewAnswerItem;
import com.PA.BackEnd.dto.interviewEvaluationItem;
import com.PA.BackEnd.dto.interviewEvaluationRequest;
import com.PA.BackEnd.dto.interviewEvaluationResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class interviewEvaluationService {
    public interviewEvaluationResponse evaluate(interviewEvaluationRequest request) {
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("answers are required for evaluation");
        }

        List<interviewEvaluationItem> items = new ArrayList<>();
        int total = 0;
        for (interviewAnswerItem answer : request.getAnswers()) {
            interviewEvaluationItem item = evaluateSingle(answer);
            items.add(item);
            total += item.getOverallScore();
        }

        int overall = Math.round(total / (float) items.size());
        interviewEvaluationResponse response = new interviewEvaluationResponse();
        response.setTargetRole(request.getTargetRole());
        response.setOverallScore(overall);
        response.setSummary(summaryForScore(overall));
        response.setEvaluations(items);
        response.setNextActions(nextActions(overall, items));
        response.setEvaluatedAt(Instant.now());
        return response;
    }

    private interviewEvaluationItem evaluateSingle(interviewAnswerItem answer) {
        String userAnswer = safe(answer.getUserAnswer());
        String expectedFocus = safe(answer.getExpectedFocus());
        String skill = safe(answer.getSkill());

        int relevance = relevanceScore(userAnswer, expectedFocus, skill);
        int technical = technicalScore(userAnswer);
        int clarity = clarityScore(userAnswer);
        int overall = Math.round((relevance * 0.4f) + (technical * 0.4f) + (clarity * 0.2f));

        interviewEvaluationItem item = new interviewEvaluationItem();
        item.setQuestionId(safe(answer.getQuestionId()));
        item.setSkill(skill);
        item.setRelevanceScore(relevance);
        item.setTechnicalScore(technical);
        item.setClarityScore(clarity);
        item.setOverallScore(overall);
        item.setStrengths(buildStrengths(relevance, technical, clarity));
        item.setImprovements(buildImprovements(relevance, technical, clarity));
        item.setFeedback(summaryForScore(overall));
        return item;
    }

    private int relevanceScore(String answer, String expectedFocus, String skill) {
        if (answer.isBlank()) {
            return 0;
        }

        int score = 45;
        if (!skill.isBlank() && containsToken(answer, skill)) {
            score += 20;
        }

        int expectedMatches = keywordMatches(answer, expectedFocus);
        score += Math.min(25, expectedMatches * 8);

        if (containsAny(answer, List.of("step", "approach", "diagnose", "trade-off", "design"))) {
            score += 10;
        }
        return clamp100(score);
    }

    private int technicalScore(String answer) {
        if (answer.isBlank()) {
            return 0;
        }

        int score = 35;
        if (containsAny(answer, List.of("complexity", "latency", "throughput", "index", "cache", "scal")) ) {
            score += 25;
        }
        if (containsAny(answer, List.of("test", "monitor", "rollback", "logging", "metrics"))) {
            score += 20;
        }
        if (answer.length() > 180) {
            score += 10;
        }
        if (containsAny(answer, List.of("because", "therefore", "trade-off"))) {
            score += 10;
        }
        return clamp100(score);
    }

    private int clarityScore(String answer) {
        if (answer.isBlank()) {
            return 0;
        }
        int score = 40;
        if (answer.length() > 120) {
            score += 20;
        }
        if (containsAny(answer, List.of("1.", "2.", "first", "second", "finally"))) {
            score += 20;
        }
        if (containsAny(answer, List.of(".", ";", ":"))) {
            score += 10;
        }
        if (answer.length() > 500) {
            score += 10;
        }
        return clamp100(score);
    }

    private List<String> buildStrengths(int relevance, int technical, int clarity) {
        List<String> strengths = new ArrayList<>();
        if (relevance >= 70) {
            strengths.add("Answer stayed relevant to the problem statement.");
        }
        if (technical >= 70) {
            strengths.add("Technical depth was strong and practical.");
        }
        if (clarity >= 70) {
            strengths.add("Communication was clear and structured.");
        }
        if (strengths.isEmpty()) {
            strengths.add("Attempted to address the question directly.");
        }
        return strengths;
    }

    private List<String> buildImprovements(int relevance, int technical, int clarity) {
        List<String> improvements = new ArrayList<>();
        if (relevance < 65) {
            improvements.add("Tie the answer more closely to the asked skill and scenario.");
        }
        if (technical < 65) {
            improvements.add("Add concrete technical details, trade-offs, and validation steps.");
        }
        if (clarity < 65) {
            improvements.add("Present the solution in a clearer step-by-step structure.");
        }
        if (improvements.isEmpty()) {
            improvements.add("Increase conciseness while preserving depth.");
        }
        return improvements;
    }

    private List<String> nextActions(int overall, List<interviewEvaluationItem> items) {
        List<String> actions = new ArrayList<>();
        if (overall < 60) {
            actions.add("Rehearse fundamentals for weakest skills and re-answer with a structured format.");
        } else if (overall < 80) {
            actions.add("Strengthen technical depth and include explicit trade-off discussion.");
        } else {
            actions.add("Move to timed mock interviews and system-design follow-ups.");
        }

        interviewEvaluationItem weakest = null;
        for (interviewEvaluationItem item : items) {
            if (weakest == null || item.getOverallScore() < weakest.getOverallScore()) {
                weakest = item;
            }
        }
        if (weakest != null && weakest.getSkill() != null && !weakest.getSkill().isBlank()) {
            actions.add("Prioritize another practice round on: " + weakest.getSkill());
        }
        return actions;
    }

    private String summaryForScore(int score) {
        if (score >= 85) {
            return "Strong interview performance with good technical communication.";
        }
        if (score >= 70) {
            return "Solid baseline; refine depth and structure for stronger interview impact.";
        }
        if (score >= 55) {
            return "Moderate readiness; focus on fundamentals and problem-solving narrative.";
        }
        return "Needs improvement before interviews; rebuild answers with clear technical structure.";
    }

    private int keywordMatches(String answer, String expectedFocus) {
        if (expectedFocus.isBlank()) {
            return 0;
        }
        int matches = 0;
        for (String token : expectedFocus.toLowerCase(Locale.ROOT).split("[^a-z0-9+#]+")) {
            if (token.length() > 4 && containsToken(answer, token)) {
                matches++;
            }
        }
        return matches;
    }

    private boolean containsAny(String text, List<String> tokens) {
        for (String token : tokens) {
            if (containsToken(text, token)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsToken(String text, String token) {
        if (text == null || token == null || token.isBlank()) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(token.toLowerCase(Locale.ROOT));
    }

    private int clamp100(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
