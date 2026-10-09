package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.AssessmentEventType;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessmentEvent;

import java.time.Instant;
import java.util.UUID;

public record OnlineAssessmentEventResponse(
    UUID id,
    UUID assessmentId,
    AssessmentStatus previousStatus,
    AssessmentStatus newStatus,
    AssessmentEventType eventType,
    String notes,
    String source,
    Instant occurredAt
) {
    public static OnlineAssessmentEventResponse fromDomain(OnlineAssessmentEvent event) {
        return new OnlineAssessmentEventResponse(
            event.getId(),
            event.getAssessmentId(),
            event.getPreviousStatus(),
            event.getNewStatus(),
            event.getEventType(),
            event.getNotes(),
            event.getSource(),
            event.getOccurredAt()
        );
    }
}
