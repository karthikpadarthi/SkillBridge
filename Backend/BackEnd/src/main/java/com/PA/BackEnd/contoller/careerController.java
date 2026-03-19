package com.PA.BackEnd.contoller;

import com.PA.BackEnd.dto.analyzeRequest;
import com.PA.BackEnd.dto.gapAnalysisResponse;
import com.PA.BackEnd.dto.interviewQuestionRequest;
import com.PA.BackEnd.dto.interviewQuestionResponse;
import com.PA.BackEnd.dto.interviewEvaluationRequest;
import com.PA.BackEnd.dto.interviewEvaluationResponse;
import com.PA.BackEnd.dto.jobIngestRequest;
import com.PA.BackEnd.dto.jobIngestResponse;
import com.PA.BackEnd.dto.metricsSummaryResponse;
import com.PA.BackEnd.dto.roadmapRequest;
import com.PA.BackEnd.dto.roadmapResponse;
import com.PA.BackEnd.model.interviewQuestionSet;
import com.PA.BackEnd.model.jobRole;
import com.PA.BackEnd.model.roadmapPlan;
import com.PA.BackEnd.model.userProfileSnapshot;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.PA.BackEnd.repository.interviewQuestionSetRepository;
import com.PA.BackEnd.repository.jobRepository;
import com.PA.BackEnd.repository.roadmapPlanRepository;
import com.PA.BackEnd.repository.userProfileSnapshotRepository;
import com.PA.BackEnd.service.analysisService;
import com.PA.BackEnd.service.interviewEvaluationService;
import com.PA.BackEnd.service.interviewQuestionService;
import com.PA.BackEnd.service.jobIngestionService;
import com.PA.BackEnd.service.metricsService;
import com.PA.BackEnd.service.pdfTextExtractionService;
import com.PA.BackEnd.service.roadmapService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class careerController {
    private analysisService analysisService;
    private jobRepository jobRepository;
    private jobIngestionService jobIngestionService;
    private roadmapService roadmapService;
    private interviewQuestionService interviewQuestionService;
    private interviewEvaluationService interviewEvaluationService;
    private roadmapPlanRepository roadmapPlanRepository;
    private interviewQuestionSetRepository interviewQuestionSetRepository;
    private userProfileSnapshotRepository userProfileSnapshotRepository;
    private metricsService metricsService;
    private pdfTextExtractionService pdfTextExtractionService;

    public careerController(analysisService analysisService,
                            jobRepository jobRepository,
                            jobIngestionService jobIngestionService,
                            roadmapService roadmapService,
                            interviewQuestionService interviewQuestionService,
                            interviewEvaluationService interviewEvaluationService,
                            metricsService metricsService,
                            pdfTextExtractionService pdfTextExtractionService,
                            roadmapPlanRepository roadmapPlanRepository,
                            interviewQuestionSetRepository interviewQuestionSetRepository,
                            userProfileSnapshotRepository userProfileSnapshotRepository) {
        this.analysisService = analysisService;
        this.jobRepository = jobRepository;
        this.jobIngestionService = jobIngestionService;
        this.roadmapService = roadmapService;
        this.interviewQuestionService = interviewQuestionService;
        this.interviewEvaluationService = interviewEvaluationService;
        this.metricsService = metricsService;
        this.pdfTextExtractionService = pdfTextExtractionService;
        this.roadmapPlanRepository = roadmapPlanRepository;
        this.interviewQuestionSetRepository = interviewQuestionSetRepository;
        this.userProfileSnapshotRepository = userProfileSnapshotRepository;
    }

    @PostMapping("/analyze")
    public gapAnalysisResponse analyze(@RequestBody analyzeRequest request, Authentication authentication) {
        gapAnalysisResponse response = analysisService.analyze(request);
        metricsService.recordAnalyze(authentication.getName(), response.getTargetRole(), response.getCoverageScore(), response.getAnalysisMode());
        return response;
    }

    @PostMapping("/resume/analyze")
    public gapAnalysisResponse analyzeResumePdf(@RequestParam("resumeFile") MultipartFile resumeFile,
                                                @RequestParam("targetRole") String targetRole,
                                                @RequestParam(value = "githubLikeText", required = false) String githubLikeText,
                                                @RequestParam(value = "currentSkills", required = false) List<String> currentSkills,
                                                Authentication authentication) {
        analyzeRequest request = new analyzeRequest();
        request.setResumeText(pdfTextExtractionService.extractText(resumeFile));
        request.setTargetRole(targetRole);
        request.setGithubLikeText(githubLikeText);
        request.setCurrentSkills(currentSkills == null ? List.of() : currentSkills);

        gapAnalysisResponse response = analysisService.analyze(request);
        metricsService.recordAnalyze(authentication.getName(), response.getTargetRole(), response.getCoverageScore(), response.getAnalysisMode());
        return response;
    }

    @GetMapping("/roles")
    public List<jobRole> getRoles() {
        return jobRepository.findAll();
    }

    @PostMapping("/jobs/ingest")
    public jobIngestResponse ingestJobs(@RequestBody jobIngestRequest request) {
        return jobIngestionService.ingest(request);
    }

    @PostMapping("/jobs/aggregate/{targetRole}")
    public jobIngestResponse aggregateRole(@PathVariable String targetRole) {
        return jobIngestionService.aggregateRoleSkills(targetRole);
    }

    @PostMapping("/roadmap/generate")
    public roadmapResponse generateRoadmap(@RequestBody roadmapRequest request, Authentication authentication) {
        roadmapResponse response = roadmapService.generateRoadmap(request, authentication.getName());
        metricsService.recordRoadmap(authentication.getName(), response.getTargetRole(), response.getBaselineCoverageScore(), response.getMilestones().size());
        return response;
    }

    @PostMapping("/resume/roadmap")
    public roadmapResponse generateRoadmapFromResumePdf(@RequestParam("resumeFile") MultipartFile resumeFile,
                                                         @RequestParam("targetRole") String targetRole,
                                                         @RequestParam(value = "githubLikeText", required = false) String githubLikeText,
                                                         @RequestParam(value = "currentSkills", required = false) List<String> currentSkills,
                                                         @RequestParam(value = "durationWeeks", defaultValue = "8") int durationWeeks,
                                                         @RequestParam(value = "weeklyHours", defaultValue = "8") int weeklyHours,
                                                         @RequestParam(value = "includePaidResources", defaultValue = "false") boolean includePaidResources,
                                                         Authentication authentication) {
        analyzeRequest profile = new analyzeRequest();
        profile.setResumeText(pdfTextExtractionService.extractText(resumeFile));
        profile.setTargetRole(targetRole);
        profile.setGithubLikeText(githubLikeText);
        profile.setCurrentSkills(currentSkills == null ? List.of() : currentSkills);

        roadmapRequest request = new roadmapRequest();
        request.setProfile(profile);
        request.setDurationWeeks(durationWeeks);
        request.setWeeklyHours(weeklyHours);
        request.setIncludePaidResources(includePaidResources);

        roadmapResponse response = roadmapService.generateRoadmap(request, authentication.getName());
        metricsService.recordRoadmap(authentication.getName(), response.getTargetRole(), response.getBaselineCoverageScore(), response.getMilestones().size());
        return response;
    }

    @PostMapping("/interview/questions")
    public interviewQuestionResponse generateInterviewQuestions(@RequestBody interviewQuestionRequest request,
                                                               Authentication authentication) {
        interviewQuestionResponse response = interviewQuestionService.generateQuestions(request, authentication.getName());
        metricsService.recordInterviewSet(authentication.getName(), response.getTargetRole(), response.getQuestions().size(), response.getBaselineCoverageScore());
        return response;
    }

    @PostMapping("/interview/evaluate")
    public interviewEvaluationResponse evaluateInterviewAnswers(@RequestBody interviewEvaluationRequest request,
                                                                Authentication authentication) {
        interviewEvaluationResponse response = interviewEvaluationService.evaluate(request);
        metricsService.recordInterviewEvaluation(authentication.getName(), request.getTargetRole(), response.getOverallScore(), request.getAnswers().size());
        return response;
    }

    @GetMapping("/history/me/roadmaps")
    public List<roadmapPlan> getRoadmapHistory(Authentication authentication) {
        return roadmapPlanRepository.findByUserIdOrderByGeneratedAtDesc(authentication.getName());
    }

    @GetMapping("/history/me/interviews")
    public List<interviewQuestionSet> getInterviewHistory(Authentication authentication) {
        return interviewQuestionSetRepository.findByUserIdOrderByGeneratedAtDesc(authentication.getName());
    }

    @GetMapping("/history/me/profiles")
    public List<userProfileSnapshot> getProfileHistory(Authentication authentication) {
        return userProfileSnapshotRepository.findByUserIdOrderByCreatedAtDesc(authentication.getName());
    }

    @GetMapping("/metrics/me/summary")
    public metricsSummaryResponse getMetricsSummary(Authentication authentication) {
        return metricsService.getSummaryForUser(authentication.getName());
    }
}
