package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.*;

import java.time.Instant;
import java.util.UUID;

public record InterviewSummaryResponse(
    UUID id,
    UUID applicationId,
    UUID jobId,
    String jobTitle,
    String companyName,
    String jobLocation,
    InterviewRound round,
    int roundNumber,
    InterviewFormat format,
    InterviewStatus status,
    InterviewOutcome outcome,
    Instant scheduledStartTime,
    Instant scheduledEndTime,
    String timeZone,
    String meetingLink,
    String location,
    String interviewerNames,
    int preparationCount
) {
    public static InterviewSummaryResponse fromDomain(Interview domain, String jobTitle, String companyName, String jobLocation) {
        return new InterviewSummaryResponse(
            domain.getId(),
            domain.getApplicationId(),
            domain.getJobId(),
            jobTitle,
            companyName,
            jobLocation,
            domain.getRound(),
            domain.getRoundNumber(),
            domain.getFormat(),
            domain.getStatus(),
            domain.getOutcome(),
            domain.getScheduledStartTime(),
            domain.getScheduledEndTime(),
            domain.getTimeZone(),
            domain.getMeetingLink(),
            domain.getLocation(),
            domain.getInterviewerNames(),
            domain.getPreparations() != null ? domain.getPreparations().size() : 0
        );
    }
}
