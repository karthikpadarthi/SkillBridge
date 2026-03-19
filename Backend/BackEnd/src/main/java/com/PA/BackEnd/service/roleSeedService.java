package com.PA.BackEnd.service;

import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.repository.jobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class roleSeedService implements CommandLineRunner {
    private final jobRepository jobRepository;

    public roleSeedService(jobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public void run(String... args) {
        if (jobRepository.count() > 0) {
            return;
        }

        Map<String, Integer> backendDemand = new LinkedHashMap<>();
        backendDemand.put("Java", 92);
        backendDemand.put("Spring Boot", 89);
        backendDemand.put("SQL", 85);
        backendDemand.put("Microservices", 82);
        backendDemand.put("Docker", 76);
        backendDemand.put("REST APIs", 88);

        Map<String, Integer> frontendDemand = new LinkedHashMap<>();
        frontendDemand.put("React", 91);
        frontendDemand.put("JavaScript", 93);
        frontendDemand.put("HTML", 84);
        frontendDemand.put("CSS", 82);
        frontendDemand.put("TypeScript", 79);
        frontendDemand.put("State Management", 74);

        Map<String, Integer> cloudDemand = new LinkedHashMap<>();
        cloudDemand.put("AWS", 91);
        cloudDemand.put("Linux", 80);
        cloudDemand.put("Terraform", 76);
        cloudDemand.put("CI/CD", 84);
        cloudDemand.put("Docker", 81);
        cloudDemand.put("Kubernetes", 79);

        jobRepository.saveAll(List.of(
                new jobRole(
                        "Backend Engineer",
                        List.of("Java", "Spring Boot", "SQL", "Microservices", "Docker", "REST APIs"),
                        backendDemand
                ),
                new jobRole(
                        "Frontend Engineer",
                        List.of("React", "JavaScript", "HTML", "CSS", "TypeScript", "State Management"),
                        frontendDemand
                ),
                new jobRole(
                        "Cloud Engineer",
                        List.of("AWS", "Linux", "Terraform", "CI/CD", "Docker", "Kubernetes"),
                        cloudDemand
                )
        ));
    }
}
