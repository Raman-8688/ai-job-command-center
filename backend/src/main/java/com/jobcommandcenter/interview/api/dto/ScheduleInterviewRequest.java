package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewFormat;
import com.jobcommandcenter.interview.domain.InterviewRound;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ScheduleInterviewRequest(
    @NotNull(message = "Job ID is required")
    UUID jobId,
    UUID applicationId,
    @NotNull(message = "Interview round is required")
    InterviewRound round,
    Integer roundNumber,
    @NotNull(message = "Interview format is required")
    InterviewFormat format,
    @NotNull(message = "Scheduled start time is required")
    Instant scheduledStartTime,
    @NotNull(message = "Scheduled end time is required")
    Instant scheduledEndTime,
    String timeZone,
    String meetingLink,
    String location,
    String interviewerNames,
    String interviewerRoles,
    String notes
) {}
