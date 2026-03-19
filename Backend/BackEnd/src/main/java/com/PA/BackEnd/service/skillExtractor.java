package com.PA.BackEnd.service;

public interface skillExtractor {
    skillExtractionResult extractSkills(String resumeText, String githubLikeText);
}
