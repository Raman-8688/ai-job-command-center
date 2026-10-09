package com.jobcommandcenter.application.infrastructure;

import com.jobcommandcenter.application.domain.ApplicationEventType;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.EventSource;
import com.jobcommandcenter.application.domain.JobApplicationEvent;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for job_application_events table.
 */
@Entity
@Table(name = "job_application_events")
public class JobApplicationEventJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "application_id", nullable = false, insertable = false, updatable = false)
    private UUID applicationId;


    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private ApplicationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private ApplicationStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ApplicationEventType eventType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private EventSource source;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public JobApplicationEventJpaEntity() {}

    public static JobApplicationEventJpaEntity fromDomain(JobApplicationEvent domain) {
        JobApplicationEventJpaEntity entity = new JobApplicationEventJpaEntity();
        entity.id = domain.getId();
        entity.applicationId = domain.getApplicationId();
        entity.previousStatus = domain.getPreviousStatus();
        entity.newStatus = domain.getNewStatus();
        entity.eventType = domain.getEventType();
        entity.notes = domain.getNotes();
        entity.source = domain.getSource();
        entity.occurredAt = domain.getOccurredAt();
        return entity;
    }

    public JobApplicationEvent toDomain() {
        return new JobApplicationEvent(
            this.id,
            this.applicationId,
            this.previousStatus,
            this.newStatus,
            this.eventType,
            this.notes,
            this.source,
            this.occurredAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public ApplicationStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(ApplicationStatus previousStatus) { this.previousStatus = previousStatus; }
    public ApplicationStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ApplicationStatus newStatus) { this.newStatus = newStatus; }
    public ApplicationEventType getEventType() { return eventType; }
    public void setEventType(ApplicationEventType eventType) { this.eventType = eventType; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public EventSource getSource() { return source; }
    public void setSource(EventSource source) { this.source = source; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
