package com.jobcommandcenter.application.domain;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate root managing the lifecycle, resume linkage, and timeline events of a Job Application.
 */
public class JobApplication {

    private final UUID id;
    private final UUID userId;
    private final UUID jobId;
    private UUID resumeId;
    private UUID tailoredResumeId;
    private ApplicationStatus status;
    private Instant appliedAt;
    private ApplicationSource submissionSource;
    private String externalReference;
    private Instant nextFollowUpDate;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;
    private final List<JobApplicationEvent> events;

    public JobApplication(
        UUID id,
        UUID userId,
        UUID jobId,
        UUID resumeId,
        UUID tailoredResumeId,
        ApplicationStatus status,
        Instant appliedAt,
        ApplicationSource submissionSource,
        String externalReference,
        Instant nextFollowUpDate,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        long version,
        List<JobApplicationEvent> events
    ) {
        this.id = Objects.requireNonNull(id, "Application ID must not be null");
        this.userId = Objects.requireNonNull(userId, "User ID must not be null");
        this.jobId = Objects.requireNonNull(jobId, "Job ID must not be null");
        this.resumeId = resumeId;
        this.tailoredResumeId = tailoredResumeId;
        this.status = status != null ? status : ApplicationStatus.DRAFT;
        this.appliedAt = appliedAt;
        this.submissionSource = submissionSource != null ? submissionSource : ApplicationSource.MANUAL;
        this.externalReference = externalReference;
        this.nextFollowUpDate = nextFollowUpDate;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.version = version;
        this.events = events != null ? new ArrayList<>(events) : new ArrayList<>();
    }

    public static JobApplication createNew(
        UUID userId,
        UUID jobId,
        ApplicationStatus initialStatus,
        Instant appliedAt,
        ApplicationSource source,
        String externalReference,
        UUID resumeId,
        UUID tailoredResumeId,
        String notes
    ) {
        Instant now = Instant.now();
        ApplicationStatus status = initialStatus != null ? initialStatus : ApplicationStatus.DRAFT;
        Instant effectiveAppliedAt = appliedAt;
        if (effectiveAppliedAt == null && status != ApplicationStatus.DRAFT) {
            effectiveAppliedAt = now;
        }

        UUID appId = UUID.randomUUID();
        JobApplication application = new JobApplication(
            appId,
            userId,
            jobId,
            resumeId,
            tailoredResumeId,
            status,
            effectiveAppliedAt,
            source != null ? source : ApplicationSource.MANUAL,
            externalReference,
            null,
            notes,
            now,
            now,
            0L,
            new ArrayList<>()
        );

        // Record initial event
        JobApplicationEvent initialEvent = JobApplicationEvent.create(
            appId,
            null,
            status,
            ApplicationEventType.CREATED,
            "Application initialized in " + status + " status",
            EventSource.USER
        );
        application.events.add(initialEvent);

        return application;
    }

    public void transitionTo(ApplicationStatus newStatus, String eventNotes, EventSource source) {
        transitionTo(newStatus, eventNotes, source, Instant.now());
    }

    public void transitionTo(ApplicationStatus newStatus, String eventNotes, EventSource source, Instant occurredAt) {
        Objects.requireNonNull(newStatus, "Target status cannot be null");
        if (this.status == newStatus) {
            return;
        }

        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStateTransitionException(this.status, newStatus);
        }

        ApplicationStatus previous = this.status;
        this.status = newStatus;
        if (newStatus == ApplicationStatus.APPLIED && this.appliedAt == null) {
            this.appliedAt = occurredAt != null ? occurredAt : Instant.now();
        }
        this.updatedAt = Instant.now();

        ApplicationEventType eventType = (previous == ApplicationStatus.REJECTED || previous == ApplicationStatus.WITHDRAWN || previous == ApplicationStatus.ARCHIVED)
            ? ApplicationEventType.REOPENED
            : ApplicationEventType.STATUS_CHANGED;

        JobApplicationEvent event = JobApplicationEvent.create(
            this.id,
            previous,
            newStatus,
            eventType,
            eventNotes != null ? eventNotes : "Status changed from " + previous + " to " + newStatus,
            source != null ? source : EventSource.USER,
            occurredAt != null ? occurredAt : Instant.now()
        );
        this.events.add(event);
    }

    public void linkResume(UUID masterResumeId, UUID tailoredResumeVariantId, String notes, EventSource source) {
        this.resumeId = masterResumeId;
        this.tailoredResumeId = tailoredResumeVariantId;
        this.updatedAt = Instant.now();

        JobApplicationEvent event = JobApplicationEvent.create(
            this.id,
            this.status,
            this.status,
            ApplicationEventType.RESUME_LINKED,
            notes != null ? notes : "Resume version linked to application",
            source != null ? source : EventSource.USER
        );
        this.events.add(event);
    }

    public void updateEditableDetails(ApplicationSource source, String externalRef, Instant appliedAt, Instant nextFollowUp, String notes) {
        if (source != null) {
            this.submissionSource = source;
        }
        this.externalReference = externalRef;
        if (appliedAt != null) {
            this.appliedAt = appliedAt;
        }
        this.nextFollowUpDate = nextFollowUp;
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public void addNote(String noteContent, EventSource source) {
        if (noteContent == null || noteContent.isBlank()) {
            return;
        }
        JobApplicationEvent event = JobApplicationEvent.create(
            this.id,
            this.status,
            this.status,
            ApplicationEventType.NOTE_ADDED,
            noteContent.trim(),
            source != null ? source : EventSource.USER
        );
        this.events.add(event);
        this.updatedAt = Instant.now();
    }

    public static boolean isValidTransition(ApplicationStatus from, ApplicationStatus to) {
        if (from == to) return true;
        return switch (from) {
            case DRAFT -> (to == ApplicationStatus.APPLIED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case APPLIED -> (to == ApplicationStatus.SCREENING || to == ApplicationStatus.ASSESSMENT || to == ApplicationStatus.INTERVIEW || to == ApplicationStatus.OFFER || to == ApplicationStatus.REJECTED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case SCREENING -> (to == ApplicationStatus.ASSESSMENT || to == ApplicationStatus.INTERVIEW || to == ApplicationStatus.OFFER || to == ApplicationStatus.REJECTED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case ASSESSMENT -> (to == ApplicationStatus.INTERVIEW || to == ApplicationStatus.OFFER || to == ApplicationStatus.REJECTED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case INTERVIEW -> (to == ApplicationStatus.ASSESSMENT || to == ApplicationStatus.OFFER || to == ApplicationStatus.REJECTED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case OFFER -> (to == ApplicationStatus.ACCEPTED || to == ApplicationStatus.REJECTED || to == ApplicationStatus.WITHDRAWN || to == ApplicationStatus.ARCHIVED);
            case ACCEPTED -> (to == ApplicationStatus.ARCHIVED || to == ApplicationStatus.WITHDRAWN);
            case REJECTED, WITHDRAWN, ARCHIVED -> (to == ApplicationStatus.DRAFT || to == ApplicationStatus.APPLIED || to == ApplicationStatus.SCREENING);
        };
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getJobId() { return jobId; }
    public UUID getResumeId() { return resumeId; }
    public UUID getTailoredResumeId() { return tailoredResumeId; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getAppliedAt() { return appliedAt; }
    public ApplicationSource getSubmissionSource() { return submissionSource; }
    public String getExternalReference() { return externalReference; }
    public Instant getNextFollowUpDate() { return nextFollowUpDate; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public List<JobApplicationEvent> getEvents() { return Collections.unmodifiableList(events); }
}
