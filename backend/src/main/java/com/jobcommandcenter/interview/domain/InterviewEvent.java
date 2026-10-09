package com.jobcommandcenter.interview.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable historical record of state changes, rescheduling, and notes on an interview.
 */
public class InterviewEvent {

    private final UUID id;
    private final UUID interviewId;
    private final InterviewStatus previousStatus;
    private final InterviewStatus newStatus;
    private final InterviewEventType eventType;
    private final String notes;
    private final String source;
    private final Instant occurredAt;

    public InterviewEvent(UUID id, UUID interviewId, InterviewStatus previousStatus, InterviewStatus newStatus,
                          InterviewEventType eventType, String notes, String source, Instant occurredAt) {
        this.id = Objects.requireNonNull(id, "Event id must not be null");
        this.interviewId = Objects.requireNonNull(interviewId, "Interview id must not be null");
        this.newStatus = Objects.requireNonNull(newStatus, "New status must not be null");
        this.eventType = Objects.requireNonNull(eventType, "Event type must not be null");
        this.previousStatus = previousStatus;
        this.notes = notes;
        this.source = (source != null && !source.isBlank()) ? source : "USER";
        this.occurredAt = (occurredAt != null) ? occurredAt : Instant.now();
    }

    public static InterviewEvent create(UUID interviewId, InterviewStatus previousStatus, InterviewStatus newStatus,
                                        InterviewEventType eventType, String notes, String source) {
        return new InterviewEvent(
            UUID.randomUUID(),
            interviewId,
            previousStatus,
            newStatus,
            eventType,
            notes,
            source,
            Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getInterviewId() { return interviewId; }
    public InterviewStatus getPreviousStatus() { return previousStatus; }
    public InterviewStatus getNewStatus() { return newStatus; }
    public InterviewEventType getEventType() { return eventType; }
    public String getNotes() { return notes; }
    public String getSource() { return source; }
    public Instant getOccurredAt() { return occurredAt; }
}
