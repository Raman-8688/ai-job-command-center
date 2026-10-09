package com.jobcommandcenter.interview.api.dto;

import com.jobcommandcenter.interview.domain.InterviewEventType;
import com.jobcommandcenter.interview.domain.InterviewEvent;
import com.jobcommandcenter.interview.domain.InterviewStatus;

import java.time.Instant;
import java.util.UUID;

public record InterviewEventResponse(
    UUID id,
    UUID interviewId,
    InterviewStatus previousStatus,
    InterviewStatus newStatus,
    InterviewEventType eventType,
    String notes,
    String source,
    Instant occurredAt
) {
    public static InterviewEventResponse fromDomain(InterviewEvent domain) {
        return new InterviewEventResponse(
            domain.getId(),
            domain.getInterviewId(),
            domain.getPreviousStatus(),
            domain.getNewStatus(),
            domain.getEventType(),
            domain.getNotes(),
            domain.getSource(),
            domain.getOccurredAt()
        );
    }
}
