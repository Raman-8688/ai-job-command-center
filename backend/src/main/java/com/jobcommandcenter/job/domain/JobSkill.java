package com.jobcommandcenter.job.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a skill requirement associated with a job posting.
 */
public class JobSkill {

    private final UUID id;
    private final UUID jobId;
    private final UUID skillId;
    private final SkillRequirementType requirementType;
    private final BigDecimal yearsExperienceRequired;
    private final Instant createdAt;

    public JobSkill(UUID id,
                    UUID jobId,
                    UUID skillId,
                    SkillRequirementType requirementType,
                    BigDecimal yearsExperienceRequired,
                    Instant createdAt) {
        this.id = Objects.requireNonNull(id, "JobSkill ID cannot be null");
        this.jobId = Objects.requireNonNull(jobId, "Job ID cannot be null");
        this.skillId = Objects.requireNonNull(skillId, "Skill ID cannot be null");
        this.requirementType = requirementType != null ? requirementType : SkillRequirementType.REQUIRED;
        this.yearsExperienceRequired = yearsExperienceRequired != null ? yearsExperienceRequired : BigDecimal.ZERO;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static JobSkill create(UUID jobId,
                                  UUID skillId,
                                  SkillRequirementType requirementType,
                                  BigDecimal yearsExperienceRequired) {
        return new JobSkill(
            UUID.randomUUID(),
            jobId,
            skillId,
            requirementType,
            yearsExperienceRequired,
            Instant.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getJobId() { return jobId; }
    public UUID getSkillId() { return skillId; }
    public SkillRequirementType getRequirementType() { return requirementType; }
    public BigDecimal getYearsExperienceRequired() { return yearsExperienceRequired; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isRequired() {
        return this.requirementType == SkillRequirementType.REQUIRED;
    }

    public boolean isPreferred() {
        return this.requirementType == SkillRequirementType.PREFERRED;
    }
}
