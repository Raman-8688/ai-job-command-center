package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.UserJobStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_jobs", uniqueConstraints = {
    @UniqueConstraint(name = "uq_user_jobs_user_job", columnNames = {"user_id", "job_id"})
})
public class UserJobJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private UserJobStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserJobJpaEntity() {}

    public UserJobJpaEntity(UUID id,
                            UUID userId,
                            UUID jobId,
                            UserJobStatus status,
                            String notes,
                            Instant discoveredAt,
                            Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
        this.notes = notes;
        this.discoveredAt = discoveredAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UserJobStatus getStatus() { return status; }
    public void setStatus(UserJobStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(Instant discoveredAt) { this.discoveredAt = discoveredAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
