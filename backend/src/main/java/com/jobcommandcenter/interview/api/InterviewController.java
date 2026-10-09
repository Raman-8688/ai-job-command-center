package com.jobcommandcenter.interview.api;

import com.jobcommandcenter.interview.api.dto.*;
import com.jobcommandcenter.interview.application.InterviewService;
import com.jobcommandcenter.interview.domain.InterviewRound;
import com.jobcommandcenter.interview.domain.InterviewSearchCriteria;
import com.jobcommandcenter.interview.domain.InterviewStatus;
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
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> scheduleInterview(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody ScheduleInterviewRequest request
    ) {
        InterviewResponse response = interviewService.scheduleInterview(securityUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InterviewSummaryResponse>> listInterviews(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) InterviewStatus status,
        @RequestParam(required = false) InterviewRound round,
        @RequestParam(required = false) UUID applicationId,
        @RequestParam(required = false) UUID jobId,
        @RequestParam(required = false) Instant scheduledAfter,
        @RequestParam(required = false) Instant scheduledBefore
    ) {
        InterviewSearchCriteria criteria = new InterviewSearchCriteria(
            status,
            round,
            applicationId,
            jobId,
            scheduledAfter,
            scheduledBefore
        );
        List<InterviewSummaryResponse> list = interviewService.listInterviews(securityUser.getId(), criteria);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<InterviewDashboardSummaryResponse> getDashboardSummary(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        InterviewDashboardSummaryResponse summary = interviewService.getDashboardSummary(securityUser.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterview(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        InterviewResponse response = interviewService.getInterviewById(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterviewResponse> updateDetails(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewDetailsRequest request
    ) {
        InterviewResponse response = interviewService.updateInterviewDetails(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<InterviewResponse> rescheduleInterview(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody RescheduleInterviewRequest request
    ) {
        InterviewResponse response = interviewService.rescheduleInterview(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<InterviewResponse> updateStatus(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewStatusRequest request
    ) {
        InterviewResponse response = interviewService.updateStatus(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/outcome")
    public ResponseEntity<InterviewResponse> updateOutcome(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewOutcomeRequest request
    ) {
        InterviewResponse response = interviewService.updateOutcome(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        interviewService.deleteInterview(securityUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ai-prep")
    public ResponseEntity<InterviewPrepBundleResponse> generateAiPrep(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        InterviewPrepBundleResponse bundle = interviewService.generateAiPrep(securityUser.getId(), id);
        return ResponseEntity.ok(bundle);
    }

    @GetMapping("/{id}/prep")
    public ResponseEntity<InterviewPrepBundleResponse> getPrepBundle(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        InterviewPrepBundleResponse bundle = interviewService.getInterviewPrepBundle(securityUser.getId(), id);
        return ResponseEntity.ok(bundle);
    }

    @PutMapping("/{id}/prep/{prepId}")
    public ResponseEntity<InterviewPreparationResponse> updatePrepNotes(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @PathVariable UUID prepId,
        @Valid @RequestBody UpdatePrepNotesRequest request
    ) {
        InterviewPreparationResponse response = interviewService.updatePrepNotes(securityUser.getId(), id, prepId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/prep")
    public ResponseEntity<InterviewPreparationResponse> addCustomPrepQuestion(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody SavePrepQuestionRequest request
    ) {
        InterviewPreparationResponse response = interviewService.addCustomPrepQuestion(securityUser.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
