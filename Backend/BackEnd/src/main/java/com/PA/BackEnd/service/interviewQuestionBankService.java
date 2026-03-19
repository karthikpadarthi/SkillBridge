package com.PA.BackEnd.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class interviewQuestionBankService {
    private final Map<String, List<String>> templates = new LinkedHashMap<>();

    public interviewQuestionBankService() {
        put("backend engineer", "java", "technical",
                "In a {role} context, explain JVM memory areas and how they impact performance in {skill}-based services.",
                "Implement a small {skill} utility and discuss trade-offs around immutability, exceptions, and concurrency.");
        put("backend engineer", "spring boot", "technical",
                "Design a layered {skill} API. How do you structure controller/service/repository boundaries?",
                "How would you secure and validate input in a {skill} REST endpoint under high traffic?");
        put("backend engineer", "sql", "scenario",
                "A {role} service using {skill} has slow queries in production. Walk through diagnosis and fix plan.",
                "How would you redesign a poorly indexed {skill} schema for better read/write balance?");

        put("cloud engineer", "aws", "technical",
                "For a {role}, compare two AWS architectures for high availability and cost trade-offs.",
                "How would you design IAM boundaries for a multi-team {skill} environment?");
        put("cloud engineer", "terraform", "technical",
                "Explain how you would structure {skill} modules for reusable infrastructure in a {role} team.",
                "How do you prevent destructive {skill} changes in CI/CD pipelines?");
        put("cloud engineer", "kubernetes", "scenario",
                "A deployment in {skill} keeps crash-looping. What is your triage path as a {role}?",
                "How would you tune autoscaling and resource requests for a noisy {skill} workload?");

        put("frontend engineer", "react", "technical",
                "In a {role} interview, explain React render lifecycle and how to avoid unnecessary re-renders.",
                "Build a {skill} component with robust state handling and error boundaries.");
        put("frontend engineer", "javascript", "technical",
                "Discuss event loop behavior and its implications in a large {skill} frontend application.",
                "How would you optimize bundle size and runtime performance for {skill}?");
        put("frontend engineer", "typescript", "scenario",
                "Your {skill} codebase has weak typing and runtime bugs. How do you migrate safely?",
                "How would you enforce type-safe API contracts in a {role} project?");
    }

    public String getQuestion(String role, String skill, String type, int index) {
        String key = key(role, skill, type);
        List<String> candidates = templates.get(key);
        if (candidates == null || candidates.isEmpty()) {
            return defaultQuestion(role, skill, type);
        }
        String template = candidates.get(Math.abs(index) % candidates.size());
        return template
                .replace("{role}", safe(role))
                .replace("{skill}", safe(skill));
    }

    public String getExpectedFocus(String role, String skill, String type) {
        if ("SCENARIO".equalsIgnoreCase(type)) {
            return "Root-cause analysis, risk mitigation, and production-safe execution for " + safe(skill) + ".";
        }
        return "Concept clarity, implementation detail, trade-offs, and testing strategy for " + safe(skill) + ".";
    }

    private String defaultQuestion(String role, String skill, String type) {
        if ("SCENARIO".equalsIgnoreCase(type)) {
            return "As a " + safe(role) + ", describe how you would resolve a production issue involving " + safe(skill) + ".";
        }
        return "Explain core concepts of " + safe(skill) + " and design a practical solution for a " + safe(role) + " role.";
    }

    private String key(String role, String skill, String type) {
        return safe(role).toLowerCase(Locale.ROOT) + "|"
                + safe(skill).toLowerCase(Locale.ROOT) + "|"
                + safe(type).toLowerCase(Locale.ROOT);
    }

    private void put(String role, String skill, String type, String first, String second) {
        templates.put(key(role, skill, type), List.of(first, second));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
