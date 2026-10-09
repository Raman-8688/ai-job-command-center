package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.job.domain.Job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record JobApplicationResponse(
    UUID id,
    UUID userId,
    UUID jobId,
    String jobTitle,
    String companyName,
    String jobLocation,
    UUID resumeId,
    UUID tailoredResumeId,
    ApplicationStatus status,
    Instant appliedAt,
    ApplicationSource submissionSource,
    String externalReference,
    Instant nextFollowUpDate,
    String notes,
    Instant createdAt,
    Instant updatedAt,
    long version,
    List<JobApplicationEventResponse> events
) {
    public static JobApplicationResponse fromDomain(JobApplication domain, Job job) {
        return new JobApplicationResponse(
            domain.getId(),
            domain.getUserId(),
            domain.getJobId(),
            job != null ? job.getTitle() : null,
            job != null ? job.getCompanyName() : null,
            job != null ? job.getLocation() : null,
            domain.getResumeId(),
            domain.getTailoredResumeId(),
            domain.getStatus(),
            domain.getAppliedAt(),
            domain.getSubmissionSource(),
            domain.getExternalReference(),
            domain.getNextFollowUpDate(),
            domain.getNotes(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            domain.getVersion(),
            domain.getEvents() != null
                ? domain.getEvents().stream().map(JobApplicationEventResponse::fromDomain).collect(Collectors.toList())
                : List.of()
        );
    }
}
