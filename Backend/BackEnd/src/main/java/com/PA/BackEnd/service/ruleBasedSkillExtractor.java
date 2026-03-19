package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.skillEvidence;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ruleBasedSkillExtractor implements skillExtractor {
    private static final List<String> SKILL_DB = List.of(
            "Java", "Spring Boot", "SQL", "Microservices", "Docker", "REST APIs",
            "React", "JavaScript", "HTML", "CSS", "TypeScript", "State Management",
            "AWS", "Linux", "Terraform", "CI/CD", "Kubernetes", "MongoDB", "Git"
    );

    @Override
    public skillExtractionResult extractSkills(String resumeText, String githubLikeText) {
        String mergedText = ((resumeText == null ? "" : resumeText) + " " + (githubLikeText == null ? "" : githubLikeText)).toLowerCase();
        Set<String> matches = new LinkedHashSet<>();
        List<skillEvidence> evidences = new ArrayList<>();

        for (String skill : SKILL_DB) {
            if (mergedText.contains(skill.toLowerCase())) {
                matches.add(skill);
                evidences.add(new skillEvidence(skill, "RULE_BASED", 0.78));
            }
        }

        return new skillExtractionResult(new ArrayList<>(matches), evidences, "FALLBACK_RULE_BASED", null);
    }
}
