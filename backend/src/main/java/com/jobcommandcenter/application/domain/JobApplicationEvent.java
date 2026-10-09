package com.jobcommandcenter.application.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable entity capturing a chronological event or state transition in the application lifecycle.
 */
public class JobApplicationEvent {

    private final UUID id;
    private final UUID applicationId;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;
    private final ApplicationEventType eventType;
    private final String notes;
    private final EventSource source;
    private final Instant occurredAt;

    public JobApplicationEvent(
        UUID id,
        UUID applicationId,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        ApplicationEventType eventType,
        String notes,
        EventSource source,
        Instant occurredAt
    ) {
        this.id = Objects.requireNonNull(id, "Event ID must not be null");
        this.applicationId = Objects.requireNonNull(applicationId, "Application ID must not be null");
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus, "New status must not be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type must not be null");
        this.notes = notes;
        this.source = source != null ? source : EventSource.USER;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static JobApplicationEvent create(
        UUID applicationId,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        ApplicationEventType eventType,
        String notes,
        EventSource source
    ) {
        return create(applicationId, previousStatus, newStatus, eventType, notes, source, Instant.now());
    }

    public static JobApplicationEvent create(
        UUID applicationId,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        ApplicationEventType eventType,
        String notes,
        EventSource source,
        Instant occurredAt
    ) {
        return new JobApplicationEvent(
            UUID.randomUUID(),
            applicationId,
            previousStatus,
            newStatus,
            eventType,
            notes,
            source,
            occurredAt != null ? occurredAt : Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getApplicationId() { return applicationId; }
    public ApplicationStatus getPreviousStatus() { return previousStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public ApplicationEventType getEventType() { return eventType; }
    public String getNotes() { return notes; }
    public EventSource getSource() { return source; }
    public Instant getOccurredAt() { return occurredAt; }
}
