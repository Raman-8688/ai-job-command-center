package com.jobcommandcenter.interview.infrastructure;

import com.jobcommandcenter.interview.domain.InterviewEventType;
import com.jobcommandcenter.interview.domain.InterviewEvent;
import com.jobcommandcenter.interview.domain.InterviewStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_events")
public class InterviewEventJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "interview_id", nullable = false, insertable = false, updatable = false)
    private UUID interviewId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private InterviewStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private InterviewStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private InterviewEventType eventType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public InterviewEventJpaEntity() {}

    public static InterviewEventJpaEntity fromDomain(InterviewEvent domain) {
        InterviewEventJpaEntity entity = new InterviewEventJpaEntity();
        entity.id = domain.getId();
        entity.interviewId = domain.getInterviewId();
        entity.previousStatus = domain.getPreviousStatus();
        entity.newStatus = domain.getNewStatus();
        entity.eventType = domain.getEventType();
        entity.notes = domain.getNotes();
        entity.source = domain.getSource();
        entity.occurredAt = domain.getOccurredAt();
        return entity;
    }

    public InterviewEvent toDomain() {
        return new InterviewEvent(
            this.id,
            this.interviewId,
            this.previousStatus,
            this.newStatus,
            this.eventType,
            this.notes,
            this.source,
            this.occurredAt
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getInterviewId() { return interviewId; }
    public void setInterviewId(UUID interviewId) { this.interviewId = interviewId; }
    public InterviewStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(InterviewStatus previousStatus) { this.previousStatus = previousStatus; }
    public InterviewStatus getNewStatus() { return newStatus; }
    public void setNewStatus(InterviewStatus newStatus) { this.newStatus = newStatus; }
    public InterviewEventType getEventType() { return eventType; }
    public void setEventType(InterviewEventType eventType) { this.eventType = eventType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
