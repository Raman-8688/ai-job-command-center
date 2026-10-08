package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.application.JobMatchingService;
import com.jobcommandcenter.job.application.JobService;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.security.jwt.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobMatchingService jobMatchingService;

    public JobController(JobService jobService, JobMatchingService jobMatchingService) {
        this.jobService = jobService;
        this.jobMatchingService = jobMatchingService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
        @AuthenticationPrincipal SecurityUser securityUser,
        @Valid @RequestBody CreateJobRequest request
    ) {
        JobResponse created = jobService.createJob(request, securityUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobSummaryResponse>> listJobs(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) JobStatus status,
        @RequestParam(required = false) JobSource source,
        @RequestParam(required = false) WorkMode workMode,
        @RequestParam(required = false) String company,
        @RequestParam(required = false) String location,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        JobSearchCriteria criteria = new JobSearchCriteria(query, status, source, workMode, location, company);
        PageResponse<JobSummaryResponse> response = jobService.searchJobs(criteria, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateJobRequest request
    ) {
        JobResponse updated = jobService.updateJob(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/match")
    public ResponseEntity<JobMatchResponse> getJobMatch(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id
    ) {
        JobMatchResult matchResult = jobMatchingService.calculateMatch(securityUser.getId(), id);
        return ResponseEntity.ok(JobMatchResponse.fromResult(matchResult));
    }

    @GetMapping("/matches")
    public ResponseEntity<List<JobMatchResponse>> getCandidateMatches(
        @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<JobMatchResult> results = jobMatchingService.calculateMatchesForUser(securityUser.getId());
        List<JobMatchResponse> responses = results.stream()
            .map(JobMatchResponse::fromResult)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/user-status")
    public ResponseEntity<Void> updateUserJobStatus(
        @AuthenticationPrincipal SecurityUser securityUser,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateUserJobStatusRequest request
    ) {
        jobService.updateUserJobStatus(securityUser.getId(), id, request.status(), request.notes());
        return ResponseEntity.noContent().build();
    }
}
