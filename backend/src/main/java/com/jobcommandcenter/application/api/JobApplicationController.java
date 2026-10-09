package com.jobcommandcenter.application.api;

import com.jobcommandcenter.application.api.dto.*;
import com.jobcommandcenter.application.application.JobApplicationService;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplicationSearchCriteria;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for job application tracking, status transitions, timeline inspection,
 * metrics summary, and AI guidance.
 */
@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    public JobApplicationController(JobApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> createApplication(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody CreateApplicationRequest request
    ) {
        JobApplicationResponse response = applicationService.createApplication(securityUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApplicationPageResponse> listApplications(
        @AuthenticationPrincipal SecurityUser securityUser,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        JobApplicationSearchCriteria criteria = new JobApplicationSearchCriteria(
            securityUser.getId(),
            status,
            search,
            page,
            size
        );
        ApplicationPageResponse response = applicationService.searchApplications(criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ApplicationDashboardSummaryResponse> getDashboardSummary(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ApplicationDashboardSummaryResponse summary = applicationService.getDashboardSummary(securityUser.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getApplication(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        JobApplicationResponse response = applicationService.getApplicationById(securityUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateApplication(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateApplicationRequest request
    ) {
        JobApplicationResponse response = applicationService.updateApplication(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<JobApplicationResponse> transitionStatus(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody TransitionStatusRequest request
    ) {
        JobApplicationResponse response = applicationService.transitionStatus(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/link-resume")
    public ResponseEntity<JobApplicationResponse> linkResume(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody LinkResumeRequest request
    ) {
        JobApplicationResponse response = applicationService.linkResume(securityUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<JobApplicationEventResponse>> getTimeline(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        List<JobApplicationEventResponse> timeline = applicationService.getTimeline(securityUser.getId(), id);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/{id}/guidance")
    public ResponseEntity<ApplicationGuidanceResponse> getGuidance(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        ApplicationGuidanceResponse guidance = applicationService.getApplicationGuidance(securityUser.getId(), id);
        return ResponseEntity.ok(guidance);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        applicationService.deleteApplication(securityUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
