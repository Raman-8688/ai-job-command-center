package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.SkillRequirementType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_skills", uniqueConstraints = {
    @UniqueConstraint(name = "uq_job_skills_job_skill", columnNames = {"job_id", "skill_id"})
})
public class JobSkillJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "job_id", insertable = false, updatable = false)
    private UUID jobId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false, length = 50)
    private SkillRequirementType requirementType;

    @Column(name = "years_experience_required", precision = 4, scale = 1)
    private BigDecimal yearsExperienceRequired;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JobSkillJpaEntity() {}

    public JobSkillJpaEntity(UUID id,
                             UUID jobId,
                             UUID skillId,
                             SkillRequirementType requirementType,
                             BigDecimal yearsExperienceRequired,
                             Instant createdAt) {
        this.id = id;
        this.jobId = jobId;
        this.skillId = skillId;
        this.requirementType = requirementType;
        this.yearsExperienceRequired = yearsExperienceRequired;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getJobId() { return jobId; }
    public void setJobId(UUID jobId) { this.jobId = jobId; }

    public UUID getSkillId() { return skillId; }
    public void setSkillId(UUID skillId) { this.skillId = skillId; }

    public SkillRequirementType getRequirementType() { return requirementType; }
    public void setRequirementType(SkillRequirementType requirementType) { this.requirementType = requirementType; }

    public BigDecimal getYearsExperienceRequired() { return yearsExperienceRequired; }
    public void setYearsExperienceRequired(BigDecimal yearsExperienceRequired) { this.yearsExperienceRequired = yearsExperienceRequired; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
