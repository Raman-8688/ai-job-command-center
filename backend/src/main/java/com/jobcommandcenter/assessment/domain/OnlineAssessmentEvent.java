package com.jobcommandcenter.assessment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable historical audit record of lifecycle state transitions, timing updates,
 * score recordings, and notes on an online assessment.
 */
public class OnlineAssessmentEvent {

    private final UUID id;
    private final UUID assessmentId;
    private final AssessmentStatus previousStatus;
    private final AssessmentStatus newStatus;
    private final AssessmentEventType eventType;
    private final String notes;
    private final String source;
    private final Instant occurredAt;

    public OnlineAssessmentEvent(UUID id, UUID assessmentId, AssessmentStatus previousStatus,
                                 AssessmentStatus newStatus, AssessmentEventType eventType,
                                 String notes, String source, Instant occurredAt) {
        this.id = Objects.requireNonNull(id, "Event id must not be null");
        this.assessmentId = Objects.requireNonNull(assessmentId, "Assessment id must not be null");
        this.newStatus = Objects.requireNonNull(newStatus, "New status must not be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type must not be null");
        this.previousStatus = previousStatus;
        this.notes = notes;
        this.source = (source != null && !source.isBlank()) ? source : "USER";
        this.occurredAt = (occurredAt != null) ? occurredAt : Instant.now();
    }

    public static OnlineAssessmentEvent create(UUID assessmentId, AssessmentStatus previousStatus,
                                               AssessmentStatus newStatus, AssessmentEventType eventType,
                                               String notes, String source) {
        return new OnlineAssessmentEvent(
            UUID.randomUUID(),
            assessmentId,
            previousStatus,
            newStatus,
            eventType,
            notes,
            source,
            Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getAssessmentId() { return assessmentId; }
    public AssessmentStatus getPreviousStatus() { return previousStatus; }
    public AssessmentStatus getNewStatus() { return newStatus; }
    public AssessmentEventType getEventType() { return eventType; }
    public String getNotes() { return notes; }
    public String getSource() { return source; }
    public Instant getOccurredAt() { return occurredAt; }
}
