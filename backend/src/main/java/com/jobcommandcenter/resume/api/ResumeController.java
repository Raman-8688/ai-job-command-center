package com.jobcommandcenter.resume.api;

import com.jobcommandcenter.resume.api.dto.*;
import com.jobcommandcenter.resume.application.JobResumeAnalysisService;
import com.jobcommandcenter.resume.application.ResumeService;
import com.jobcommandcenter.resume.domain.JobResumeAnalysisResult;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for candidate resume management and job-specific resume analysis.
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final JobResumeAnalysisService jobResumeAnalysisService;

    public ResumeController(ResumeService resumeService,
                            JobResumeAnalysisService jobResumeAnalysisService) {
        this.resumeService = resumeService;
        this.jobResumeAnalysisService = jobResumeAnalysisService;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody CreateResumeRequest request
    ) {
        ResumeResponse response = resumeService.createResume(securityUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeSummaryResponse>> getUserResumes(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<ResumeSummaryResponse> response = resumeService.getUserResumes(securityUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        ResumeResponse response = resumeService.getResume(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeResponse> updateResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateResumeRequest request
    ) {
        ResumeResponse response = resumeService.updateResume(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        resumeService.deleteResume(securityUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ResumeResponse> activateResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        ResumeResponse response = resumeService.activateResume(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ResumeResponse> archiveResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        ResumeResponse response = resumeService.archiveResume(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resumeId}/jobs/{jobId}/analysis")
    public ResponseEntity<JobResumeAnalysisResponse> getJobResumeAnalysis(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID resumeId,
        @PathVariable UUID jobId
    ) {
        JobResumeAnalysisResult result = jobResumeAnalysisService.analyzeResumeForJob(securityUser.getId(), resumeId, jobId);
        return ResponseEntity.ok(JobResumeAnalysisResponse.fromDomain(result));
    }

    @PostMapping("/{resumeId}/jobs/{jobId}/analysis")
    public ResponseEntity<JobResumeAnalysisResponse> triggerJobResumeAnalysis(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID resumeId,
        @PathVariable UUID jobId
    ) {
        JobResumeAnalysisResult result = jobResumeAnalysisService.analyzeResumeForJob(securityUser.getId(), resumeId, jobId);
        return ResponseEntity.ok(JobResumeAnalysisResponse.fromDomain(result));
    }
}
