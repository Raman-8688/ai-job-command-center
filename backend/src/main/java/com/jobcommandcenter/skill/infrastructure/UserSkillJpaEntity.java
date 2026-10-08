package com.jobcommandcenter.skill.infrastructure;

import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_skills", uniqueConstraints = {
    @UniqueConstraint(name = "uq_user_skills_user_skill", columnNames = {"user_id", "skill_id"})
})
public class UserSkillJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 50)
    private SkillProficiency proficiency;

    @Column(name = "years_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_source", nullable = false, length = 50)
    private VerificationSource verificationSource;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "last_verified_at")
    private Instant lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserSkillJpaEntity() {
    }

    public UserSkillJpaEntity(UUID id,
                              UUID userId,
                              UUID skillId,
                              SkillProficiency proficiency,
                              BigDecimal yearsExperience,
                              boolean verified,
                              VerificationSource verificationSource,
                              String notes,
                              Instant lastVerifiedAt,
                              Instant createdAt,
                              Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.skillId = skillId;
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience;
        this.verified = verified;
        this.verificationSource = verificationSource;
        this.notes = notes;
        this.lastVerifiedAt = lastVerifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getSkillId() { return skillId; }
    public void setSkillId(UUID skillId) { this.skillId = skillId; }

    public SkillProficiency getProficiency() { return proficiency; }
    public void setProficiency(SkillProficiency proficiency) { this.proficiency = proficiency; }

    public BigDecimal getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(BigDecimal yearsExperience) { this.yearsExperience = yearsExperience; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public VerificationSource getVerificationSource() { return verificationSource; }
    public void setVerificationSource(VerificationSource verificationSource) { this.verificationSource = verificationSource; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
