package com.jobcommandcenter.application.api.dto;

import com.jobcommandcenter.application.domain.ApplicationEventType;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.EventSource;
import com.jobcommandcenter.application.domain.JobApplicationEvent;

import java.time.Instant;
import java.util.UUID;

public record JobApplicationEventResponse(
    UUID id,
    UUID applicationId,
    ApplicationStatus previousStatus,
    ApplicationStatus newStatus,
    ApplicationEventType eventType,
    String notes,
    EventSource source,
    Instant occurredAt
) {
    public static JobApplicationEventResponse fromDomain(JobApplicationEvent event) {
        return new JobApplicationEventResponse(
            event.getId(),
            event.getApplicationId(),
            event.getPreviousStatus(),
            event.getNewStatus(),
            event.getEventType(),
            event.getNotes(),
            event.getSource(),
            event.getOccurredAt()
        );
    }
}
