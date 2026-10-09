package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.job.domain.Job;

import java.time.Instant;
import java.util.UUID;

public record JobApplicationSummaryResponse(
    UUID id,
    UUID jobId,
    String jobTitle,
    String companyName,
    String jobLocation,
    ApplicationStatus status,
    ApplicationSource submissionSource,
    Instant appliedAt,
    Instant nextFollowUpDate,
    Instant updatedAt,
    boolean hasResumeLinked,
    boolean hasTailoredResumeLinked
) {
    public static JobApplicationSummaryResponse fromDomain(JobApplication domain, Job job) {
        return new JobApplicationSummaryResponse(
            domain.getId(),
            domain.getJobId(),
            job != null ? job.getTitle() : "Unknown Job",
            job != null ? job.getCompanyName() : "Unknown Company",
            job != null ? job.getLocation() : null,
            domain.getStatus(),
            domain.getSubmissionSource(),
            domain.getAppliedAt(),
            domain.getNextFollowUpDate(),
            domain.getUpdatedAt(),
            domain.getResumeId() != null,
            domain.getTailoredResumeId() != null
        );
    }
}
