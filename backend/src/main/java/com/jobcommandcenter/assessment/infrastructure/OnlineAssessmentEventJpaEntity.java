package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.AssessmentEventType;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.assessment.domain.OnlineAssessmentEvent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "online_assessment_events")
public class OnlineAssessmentEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "assessment_id", nullable = false, insertable = false, updatable = false)
    private UUID assessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private AssessmentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private AssessmentStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AssessmentEventType eventType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public OnlineAssessmentEventJpaEntity() {}

    public static OnlineAssessmentEventJpaEntity fromDomain(OnlineAssessmentEvent domain) {
        OnlineAssessmentEventJpaEntity entity = new OnlineAssessmentEventJpaEntity();
        entity.id = domain.getId();
        entity.assessmentId = domain.getAssessmentId();
        entity.previousStatus = domain.getPreviousStatus();
        entity.newStatus = domain.getNewStatus();
        entity.eventType = domain.getEventType();
        entity.notes = domain.getNotes();
        entity.source = domain.getSource();
        entity.occurredAt = domain.getOccurredAt();
        return entity;
    }

    public OnlineAssessmentEvent toDomain() {
        return new OnlineAssessmentEvent(
            this.id,
            this.assessmentId,
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
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public AssessmentStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(AssessmentStatus previousStatus) { this.previousStatus = previousStatus; }
    public AssessmentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AssessmentStatus newStatus) { this.newStatus = newStatus; }
    public AssessmentEventType getEventType() { return eventType; }
    public void setEventType(AssessmentEventType eventType) { this.eventType = eventType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
