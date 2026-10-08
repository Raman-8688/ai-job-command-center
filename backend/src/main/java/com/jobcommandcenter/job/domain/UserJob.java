package com.jobcommandcenter.job.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity capturing a candidate's personal relationship and notes for a job.
 * Isolates private user actions from canonical global job data.
 */
public class UserJob {

    private final UUID id;
    private final UUID userId;
    private final UUID jobId;
    private UserJobStatus status;
    private String notes;
    private final Instant discoveredAt;
    private Instant updatedAt;

    public UserJob(UUID id,
                   UUID userId,
                   UUID jobId,
                   UserJobStatus status,
                   String notes,
                   Instant discoveredAt,
                   Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "UserJob ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.jobId = Objects.requireNonNull(jobId, "Job ID cannot be null");
        this.status = status != null ? status : UserJobStatus.DISCOVERED;
        this.notes = notes;
        this.discoveredAt = discoveredAt != null ? discoveredAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.discoveredAt;
    }

    public static UserJob create(UUID userId, UUID jobId) {
        Instant now = Instant.now();
        return new UserJob(
            UUID.randomUUID(),
            userId,
            jobId,
            UserJobStatus.DISCOVERED,
            null,
            now,
            now
        );
    }

    public void updateStatus(UserJobStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Status cannot be null");
        this.updatedAt = Instant.now();
    }

    public void updateNotes(String notes) {
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getJobId() { return jobId; }
    public UserJobStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getDiscoveredAt() { return discoveredAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
