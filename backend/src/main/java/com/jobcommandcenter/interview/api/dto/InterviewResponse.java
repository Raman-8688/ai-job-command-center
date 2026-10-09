package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record InterviewResponse(
    UUID id,
    UUID userId,
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
    String interviewerRoles,
    String notes,
    String candidateFeedback,
    Instant createdAt,
    Instant updatedAt,
    long version,
    List<InterviewEventResponse> events,
    List<InterviewPreparationResponse> preparations
) {
    public static InterviewResponse fromDomain(Interview domain, String jobTitle, String companyName, String jobLocation) {
        List<InterviewEventResponse> eventResponses = domain.getEvents() != null
            ? domain.getEvents().stream().map(InterviewEventResponse::fromDomain).collect(Collectors.toList())
            : List.of();

        List<InterviewPreparationResponse> prepResponses = domain.getPreparations() != null
            ? domain.getPreparations().stream().map(InterviewPreparationResponse::fromDomain).collect(Collectors.toList())
            : List.of();

        return new InterviewResponse(
            domain.getId(),
            domain.getUserId(),
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
            domain.getInterviewerRoles(),
            domain.getNotes(),
            domain.getCandidateFeedback(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            domain.getVersion() != null ? domain.getVersion() : 0L,
            eventResponses,
            prepResponses
        );
    }
}
