package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.learningResourceItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class learningResourceCatalogService {
    private final Map<String, List<learningResourceItem>> resourcesBySkill = new LinkedHashMap<>();

    public learningResourceCatalogService() {
        add("Java",
                new learningResourceItem("Java Programming Basics", "Coursera", "https://www.coursera.org/", false, 12),
                new learningResourceItem("Java Tutorial", "W3Schools", "https://www.w3schools.com/java/", true, 8));
        add("Spring Boot",
                new learningResourceItem("Spring Boot Fundamentals", "Spring Guides", "https://spring.io/guides", true, 10),
                new learningResourceItem("Building REST APIs with Spring", "Udemy", "https://www.udemy.com/", false, 14));
        add("SQL",
                new learningResourceItem("SQL Tutorial", "SQLBolt", "https://sqlbolt.com/", true, 6),
                new learningResourceItem("Databases for Developers", "Coursera", "https://www.coursera.org/", false, 12));
        add("Docker",
                new learningResourceItem("Docker Getting Started", "Docker", "https://docs.docker.com/get-started/", true, 6),
                new learningResourceItem("Docker Mastery", "Udemy", "https://www.udemy.com/", false, 12));
        add("Microservices",
                new learningResourceItem("Microservices Patterns", "InfoQ", "https://www.infoq.com/", true, 8),
                new learningResourceItem("Microservices with Spring Cloud", "Pluralsight", "https://www.pluralsight.com/", false, 14));
        add("REST APIs",
                new learningResourceItem("REST API Design Best Practices", "Microsoft Learn", "https://learn.microsoft.com/", true, 5),
                new learningResourceItem("API Design and Fundamentals", "Coursera", "https://www.coursera.org/", false, 8));
        add("React",
                new learningResourceItem("React Official Tutorial", "React Docs", "https://react.dev/learn", true, 8),
                new learningResourceItem("React Complete Guide", "Udemy", "https://www.udemy.com/", false, 18));
        add("AWS",
                new learningResourceItem("AWS Cloud Practitioner Essentials", "AWS Skill Builder", "https://explore.skillbuilder.aws/", true, 12),
                new learningResourceItem("AWS Solutions Architect", "A Cloud Guru", "https://acloudguru.com/", false, 20));
        add("Terraform",
                new learningResourceItem("Terraform Intro", "HashiCorp", "https://developer.hashicorp.com/terraform/tutorials", true, 7),
                new learningResourceItem("Terraform Associate Prep", "Udemy", "https://www.udemy.com/", false, 14));
        add("CI/CD",
                new learningResourceItem("CI/CD Concepts", "Atlassian", "https://www.atlassian.com/continuous-delivery", true, 6),
                new learningResourceItem("CI/CD with Jenkins", "Coursera", "https://www.coursera.org/", false, 10));
        add("Kubernetes",
                new learningResourceItem("Kubernetes Basics", "Kubernetes", "https://kubernetes.io/docs/tutorials/kubernetes-basics/", true, 8),
                new learningResourceItem("CKA Preparation", "Udemy", "https://www.udemy.com/", false, 22));
    }

    public List<learningResourceItem> getResources(String skill, boolean includePaid) {
        List<learningResourceItem> resources = resourcesBySkill.getOrDefault(skill, defaultResource(skill));
        if (includePaid) {
            return resources;
        }

        List<learningResourceItem> onlyFree = new ArrayList<>();
        for (learningResourceItem resource : resources) {
            if (resource.isFree()) {
                onlyFree.add(resource);
            }
        }
        return onlyFree.isEmpty() ? resources : onlyFree;
    }

    private List<learningResourceItem> defaultResource(String skill) {
        return List.of(
                new learningResourceItem(
                        skill + " Learning Path",
                        "Community Resources",
                        "https://roadmap.sh/",
                        true,
                        8
                )
        );
    }

    private void add(String skill, learningResourceItem first, learningResourceItem second) {
        resourcesBySkill.put(skill, List.of(first, second));
    }
}
