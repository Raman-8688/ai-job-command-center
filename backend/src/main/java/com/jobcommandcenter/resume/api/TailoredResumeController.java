package com.jobcommandcenter.resume.api;

import com.jobcommandcenter.resume.api.dto.*;
import com.jobcommandcenter.resume.application.ResumeTailoringService;
import com.jobcommandcenter.resume.domain.TailoredResume;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for job-specific resume tailoring, version management, and human review.
 */
@RestController
public class TailoredResumeController {

    private final ResumeTailoringService resumeTailoringService;

    public TailoredResumeController(ResumeTailoringService resumeTailoringService) {
        this.resumeTailoringService = resumeTailoringService;
    }

    @PostMapping("/api/resumes/{resumeId}/tailor/{jobId}")
    public ResponseEntity<TailoredResumeResponse> createTailoredDraft(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID resumeId,
        @PathVariable UUID jobId
    ) {
        TailoredResume draft = resumeTailoringService.createTailoredDraft(securityUser.getId(), resumeId, jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(TailoredResumeResponse.fromDomain(draft));
    }

    @GetMapping("/api/resumes/{resumeId}/tailored")
    public ResponseEntity<List<TailoredResumeSummaryResponse>> getTailoredResumesForResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID resumeId
    ) {
        List<TailoredResumeSummaryResponse> list = resumeTailoringService
            .getTailoredResumesForResume(securityUser.getId(), resumeId)
            .stream()
            .map(TailoredResumeSummaryResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/jobs/{jobId}/tailored-resumes")
    public ResponseEntity<List<TailoredResumeSummaryResponse>> getTailoredResumesForJob(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID jobId
    ) {
        List<TailoredResumeSummaryResponse> list = resumeTailoringService
            .getTailoredResumesForJob(securityUser.getId(), jobId)
            .stream()
            .map(TailoredResumeSummaryResponse::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/api/tailored-resumes/{tailoredResumeId}")
    public ResponseEntity<TailoredResumeResponse> getTailoredResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID tailoredResumeId
    ) {
        TailoredResume draft = resumeTailoringService.getOwnedTailoredResume(securityUser.getId(), tailoredResumeId);
        return ResponseEntity.ok(TailoredResumeResponse.fromDomain(draft));
    }

    @PutMapping("/api/tailored-resumes/{tailoredResumeId}")
    public ResponseEntity<TailoredResumeResponse> updateTailoredResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID tailoredResumeId,
        @Valid @RequestBody UpdateTailoredResumeRequest request
    ) {
        TailoredResume updated = resumeTailoringService.updateContent(
            securityUser.getId(),
            tailoredResumeId,
            request.tailoredTitle(),
            request.tailoredSummary()
        );
        return ResponseEntity.ok(TailoredResumeResponse.fromDomain(updated));
    }

    @PatchMapping("/api/tailored-resumes/{tailoredResumeId}/status")
    public ResponseEntity<TailoredResumeResponse> updateStatus(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID tailoredResumeId,
        @Valid @RequestBody UpdateTailoredResumeStatusRequest request
    ) {
        TailoredResume updated = resumeTailoringService.transitionStatus(
            securityUser.getId(),
            tailoredResumeId,
            request.status()
        );
        return ResponseEntity.ok(TailoredResumeResponse.fromDomain(updated));
    }

    @PostMapping("/api/tailored-resumes/{tailoredResumeId}/suggestions/{suggestionId}/apply")
    public ResponseEntity<TailoredResumeResponse> applySuggestion(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID tailoredResumeId,
        @PathVariable UUID suggestionId
    ) {
        TailoredResume updated = resumeTailoringService.applySuggestion(
            securityUser.getId(),
            tailoredResumeId,
            suggestionId
        );
        return ResponseEntity.ok(TailoredResumeResponse.fromDomain(updated));
    }

    @DeleteMapping("/api/tailored-resumes/{tailoredResumeId}")
    public ResponseEntity<Void> deleteTailoredResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID tailoredResumeId
    ) {
        resumeTailoringService.deleteTailoredResume(securityUser.getId(), tailoredResumeId);
        return ResponseEntity.noContent().build();
    }
}
