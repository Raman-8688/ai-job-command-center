package com.jobcommandcenter.assessment.api;

import com.jobcommandcenter.assessment.api.dto.*;
import com.jobcommandcenter.assessment.application.OnlineAssessmentService;
import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessmentSearchCriteria;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
public class OnlineAssessmentController {

    private final OnlineAssessmentService assessmentService;

    public OnlineAssessmentController(OnlineAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public ResponseEntity<OnlineAssessmentResponse> createAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody CreateAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.createAssessment(securityUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OnlineAssessmentSummaryResponse>> listAssessments(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) AssessmentStatus status,
        @RequestParam(required = false) AssessmentPlatform platform,
        @RequestParam(required = false) UUID jobId,
        @RequestParam(required = false) UUID applicationId,
        @RequestParam(required = false) Instant expiresAfter,
        @RequestParam(required = false) Instant expiresBefore
    ) {
        OnlineAssessmentSearchCriteria criteria = new OnlineAssessmentSearchCriteria(
            status,
            platform,
            jobId,
            applicationId,
            expiresAfter,
            expiresBefore
        );
        List<OnlineAssessmentSummaryResponse> list = assessmentService.listAssessments(securityUser.getId(), criteria);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<AssessmentDashboardSummaryResponse> getDashboardSummary(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        AssessmentDashboardSummaryResponse summary = assessmentService.getDashboardSummary(securityUser.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OnlineAssessmentResponse> getAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        OnlineAssessmentResponse response = assessmentService.getAssessmentById(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OnlineAssessmentResponse> updateAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.updateAssessment(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<OnlineAssessmentResponse> startAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @RequestBody(required = false) StartAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.startAssessment(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<OnlineAssessmentResponse> submitAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody SubmitAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.submitAssessment(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/result")
    public ResponseEntity<OnlineAssessmentResponse> recordResult(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody RecordAssessmentResultRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.recordResult(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/extend-deadline")
    public ResponseEntity<OnlineAssessmentResponse> extendDeadline(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody ExtendAssessmentDeadlineRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.extendDeadline(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<OnlineAssessmentResponse> expireAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @RequestBody(required = false) ExpireAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.expireAssessment(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/abandon")
    public ResponseEntity<OnlineAssessmentResponse> abandonAssessment(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @RequestBody(required = false) AbandonAssessmentRequest request
    ) {
        OnlineAssessmentResponse response = assessmentService.abandonAssessment(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/checklist")
    public ResponseEntity<AssessmentChecklistBundleResponse> getChecklist(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        AssessmentChecklistBundleResponse bundle = assessmentService.getChecklist(securityUser.getId(), id);
        return ResponseEntity.ok(bundle);
    }

    @PatchMapping("/{id}/checklist/{itemId}")
    public ResponseEntity<AssessmentChecklistBundleResponse> toggleChecklistItem(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @PathVariable UUID itemId,
        @RequestBody(required = false) ToggleChecklistItemRequest request
    ) {
        AssessmentChecklistBundleResponse bundle = assessmentService.toggleChecklistItem(securityUser.getId(), id, itemId, request);
        return ResponseEntity.ok(bundle);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<OnlineAssessmentEventResponse>> getEvents(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        List<OnlineAssessmentEventResponse> events = assessmentService.getEventHistory(securityUser.getId(), id);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{id}/briefing")
    public ResponseEntity<AssessmentBriefingResponse> generateBriefing(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @RequestBody(required = false) GenerateBriefingRequest request
    ) {
        AssessmentBriefingResponse briefing = assessmentService.generateBriefing(securityUser.getId(), id, request);
        return ResponseEntity.ok(briefing);
    }
}
