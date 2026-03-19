package com.PA.BackEnd.service;

import com.PA.BackEnd.dto.jobIngestRequest;
import com.PA.BackEnd.dto.jobIngestResponse;
import com.PA.BackEnd.dto.jobPostingInput;
import com.PA.BackEnd.model.jobPosting;
import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.repository.jobPostingRepository;
import com.PA.BackEnd.repository.jobRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class jobIngestionService {
    private final jobPostingRepository jobPostingRepository;
    private final jobRepository jobRepository;
    private final ruleBasedSkillExtractor ruleBasedSkillExtractor;

    public jobIngestionService(jobPostingRepository jobPostingRepository,
                               jobRepository jobRepository,
                               ruleBasedSkillExtractor ruleBasedSkillExtractor) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobRepository = jobRepository;
        this.ruleBasedSkillExtractor = ruleBasedSkillExtractor;
    }

    public jobIngestResponse ingest(jobIngestRequest request) {
        String roleName = sanitize(request.getTargetRole());
        ensureRoleExists(roleName);
        if (request.getPostings() == null || request.getPostings().isEmpty()) {
            throw new IllegalArgumentException("Postings cannot be empty");
        }

        List<jobPosting> existing = jobPostingRepository.findByTargetRoleIgnoreCase(roleName);
        Set<String> existingFingerprints = existing.stream()
                .map(jobPosting::getFingerprint)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());

        List<jobPosting> toPersist = new ArrayList<>();
        int duplicatesSkipped = 0;

        for (jobPostingInput input : request.getPostings()) {
            if (input.getDescription() == null || input.getDescription().isBlank()) {
                continue;
            }

            String normalizedUrl = normalizeSourceUrl(input.getSourceUrl());
            String sourceDomain = extractDomain(normalizedUrl);
            String sourceType = classifySourceType(sourceDomain, normalizedUrl);
            int reliability = reliabilityForSource(sourceType);
            String fingerprint = fingerprint(roleName, input, normalizedUrl);

            if (existingFingerprints.contains(fingerprint)) {
                duplicatesSkipped++;
                continue;
            }

            skillExtractionResult extraction = ruleBasedSkillExtractor.extractSkills(input.getDescription(), null);
            jobPosting posting = new jobPosting();
            posting.setTargetRole(roleName);
            posting.setTitle(trim(input.getTitle()));
            posting.setCompany(trim(input.getCompany()));
            posting.setDescription(input.getDescription().trim());
            posting.setSourceUrl(input.getSourceUrl());
            posting.setNormalizedSourceUrl(normalizedUrl);
            posting.setSourceDomain(sourceDomain);
            posting.setSourceType(sourceType);
            posting.setSourceReliabilityScore(reliability);
            posting.setFingerprint(fingerprint);
            posting.setExtractedSkills(extraction.getSkills());
            posting.setIngestedAt(Instant.now());
            toPersist.add(posting);
            existingFingerprints.add(fingerprint);
        }

        if (!toPersist.isEmpty()) {
            jobPostingRepository.saveAll(toPersist);
        }

        if (toPersist.isEmpty() && duplicatesSkipped == 0) {
            throw new IllegalArgumentException("No valid postings with description were provided");
        }

        return aggregateRoleSkillsInternal(roleName, toPersist.size(), duplicatesSkipped);
    }

    public jobIngestResponse aggregateRoleSkills(String targetRole) {
        String roleName = sanitize(targetRole);
        ensureRoleExists(roleName);
        return aggregateRoleSkillsInternal(roleName, 0, 0);
    }

    private jobIngestResponse aggregateRoleSkillsInternal(String roleName, int ingestedCount, int duplicatesSkipped) {
        jobRole role = ensureRoleExists(roleName);
        List<jobPosting> postings = jobPostingRepository.findByTargetRoleIgnoreCase(roleName);
        if (postings.isEmpty()) {
            throw new IllegalArgumentException("No postings found for role: " + roleName);
        }

        Map<String, Double> weightedFrequency = new LinkedHashMap<>();
        Map<String, Integer> sourceQualityBreakdown = new LinkedHashMap<>();
        sourceQualityBreakdown.put("HIGH", 0);
        sourceQualityBreakdown.put("MEDIUM", 0);
        sourceQualityBreakdown.put("LOW", 0);

        int reliabilitySum = 0;
        for (jobPosting posting : postings) {
            int reliability = posting.getSourceReliabilityScore() > 0 ? posting.getSourceReliabilityScore() : 70;
            reliabilitySum += reliability;
            incrementQualityBucket(sourceQualityBreakdown, reliability);

            double weight = Math.max(0.4, reliability / 100.0);
            Set<String> uniquePerPosting = new LinkedHashSet<>();
            for (String skill : posting.getExtractedSkills()) {
                if (skill != null && !skill.isBlank()) {
                    uniquePerPosting.add(skill.trim());
                }
            }
            for (String skill : uniquePerPosting) {
                weightedFrequency.put(skill, weightedFrequency.getOrDefault(skill, 0.0) + weight);
            }
        }

        double maxWeight = weightedFrequency.values().stream().mapToDouble(v -> v).max().orElse(1.0);
        Map<String, Integer> demandScore = weightedFrequency.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.min(100, (int) Math.round((e.getValue() / maxWeight) * 100.0)),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<String> requiredSkills = demandScore.keySet().stream().limit(12).collect(Collectors.toList());
        role.setRequiredSkills(requiredSkills);
        role.setSkillDemand(demandScore);
        jobRepository.save(role);

        jobIngestResponse response = new jobIngestResponse();
        response.setTargetRole(roleName);
        response.setIngestedCount(ingestedCount);
        response.setDuplicatesSkipped(duplicatesSkipped);
        response.setTotalPostingsForRole(postings.size());
        response.setAverageReliabilityScore((int) Math.round(reliabilitySum / (double) postings.size()));
        response.setAggregatedDemand(demandScore);
        response.setTopSkills(requiredSkills.stream().limit(6).collect(Collectors.toList()));
        response.setSourceQualityBreakdown(sourceQualityBreakdown);
        return response;
    }

    private void incrementQualityBucket(Map<String, Integer> buckets, int reliability) {
        if (reliability >= 85) {
            buckets.put("HIGH", buckets.get("HIGH") + 1);
        } else if (reliability >= 65) {
            buckets.put("MEDIUM", buckets.get("MEDIUM") + 1);
        } else {
            buckets.put("LOW", buckets.get("LOW") + 1);
        }
    }

    private int reliabilityForSource(String sourceType) {
        return switch (sourceType) {
            case "CAREERS_PAGE" -> 92;
            case "JOB_BOARD" -> 78;
            case "COMMUNITY" -> 65;
            default -> 60;
        };
    }

    private String classifySourceType(String domain, String normalizedUrl) {
        String d = domain == null ? "" : domain.toLowerCase(Locale.ROOT);
        String u = normalizedUrl == null ? "" : normalizedUrl.toLowerCase(Locale.ROOT);

        if (d.contains("linkedin") || d.contains("indeed") || d.contains("naukri")
                || d.contains("glassdoor") || d.contains("monster")) {
            return "JOB_BOARD";
        }
        if (u.contains("/careers") || u.contains("/jobs") || u.contains("/join-us")) {
            return "CAREERS_PAGE";
        }
        if (d.contains("github") || d.contains("reddit") || d.contains("medium")) {
            return "COMMUNITY";
        }
        return "UNKNOWN";
    }

    private String extractDomain(String normalizedUrl) {
        if (normalizedUrl == null || normalizedUrl.isBlank()) {
            return "unknown";
        }
        try {
            URI uri = URI.create(normalizedUrl);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return "unknown";
            }
            return host.toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private String normalizeSourceUrl(String sourceUrl) {
        String raw = trim(sourceUrl);
        if (raw.isBlank()) {
            return "";
        }
        if (!raw.toLowerCase(Locale.ROOT).startsWith("http")) {
            raw = "https://" + raw;
        }

        try {
            URI uri = URI.create(raw);
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            return scheme + "://" + host + path;
        } catch (Exception ex) {
            return raw.toLowerCase(Locale.ROOT);
        }
    }

    private String fingerprint(String roleName, jobPostingInput input, String normalizedUrl) {
        String basis = roleName.toLowerCase(Locale.ROOT) + "|"
                + trim(input.getTitle()).toLowerCase(Locale.ROOT) + "|"
                + trim(input.getCompany()).toLowerCase(Locale.ROOT) + "|"
                + normalizeDescription(input.getDescription()) + "|"
                + (normalizedUrl == null ? "" : normalizedUrl.toLowerCase(Locale.ROOT));
        return sha256Short(basis);
    }

    private String normalizeDescription(String description) {
        String value = description == null ? "" : description.trim().toLowerCase(Locale.ROOT);
        value = value.replaceAll("\\s+", " ");
        if (value.length() > 500) {
            return value.substring(0, 500);
        }
        return value;
    }

    private String sha256Short(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12 && i < bytes.length; i++) {
                sb.append(String.format("%02x", bytes[i]));
            }
            return sb.toString();
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private jobRole ensureRoleExists(String roleName) {
        Optional<jobRole> role = jobRepository.findByRoleNameIgnoreCase(roleName);
        if (role.isPresent()) {
            return role.get();
        }

        jobRole created = new jobRole();
        created.setId(roleName.toLowerCase(Locale.ROOT).replace(" ", "-"));
        created.setRoleName(roleName);
        created.setRequiredSkills(List.of());
        created.setSkillDemand(new LinkedHashMap<>());
        return jobRepository.save(created);
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("targetRole is required");
        }
        return value.trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
