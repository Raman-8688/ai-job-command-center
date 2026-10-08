package com.jobcommandcenter.skill.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a candidate's skill claim.
 * Enforces Anti-Hallucination Grounding: AI suggestions are strictly unverified by default.
 */
public class UserSkill {

    private final UUID id;
    private final UUID userId;
    private final UUID skillId;
    private SkillProficiency proficiency;
    private BigDecimal yearsExperience;
    private boolean verified;
    private VerificationSource verificationSource;
    private String notes;
    private Instant lastVerifiedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public UserSkill(UUID id,
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
        this.id = Objects.requireNonNull(id, "UserSkill ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.skillId = Objects.requireNonNull(skillId, "Skill ID cannot be null");
        this.proficiency = Objects.requireNonNull(proficiency, "Proficiency cannot be null");
        this.yearsExperience = yearsExperience != null ? yearsExperience : BigDecimal.ZERO;
        this.verificationSource = Objects.requireNonNull(verificationSource, "Verification source cannot be null");
        
        // Anti-hallucination enforcement: AI suggestions can NEVER be initially verified automatically
        if (this.verificationSource == VerificationSource.AI_SUGGESTED && verified) {
            this.verified = false;
            this.lastVerifiedAt = null;
        } else {
            this.verified = verified;
            this.lastVerifiedAt = verified ? (lastVerifiedAt != null ? lastVerifiedAt : Instant.now()) : null;
        }

        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static UserSkill createNew(UUID userId,
                                      UUID skillId,
                                      SkillProficiency proficiency,
                                      BigDecimal yearsExperience,
                                      boolean verified,
                                      VerificationSource verificationSource,
                                      String notes) {
        Instant now = Instant.now();
        return new UserSkill(
            UUID.randomUUID(),
            userId,
            skillId,
            proficiency,
            yearsExperience,
            verified,
            verificationSource,
            notes,
            verified ? now : null,
            now,
            now
        );
    }

    /**
     * Confirms and verifies this skill by user explicit action or verified proof.
     */
    public void confirmVerification(VerificationSource source) {
        if (source == VerificationSource.AI_SUGGESTED) {
            throw new IllegalArgumentException("AI suggestions cannot verify skills. Verification requires human or proven source.");
        }
        this.verified = true;
        this.verificationSource = source;
        this.lastVerifiedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Updates skill details, preserving or modifying verification status according to explicit input.
     */
    public void updateDetails(SkillProficiency proficiency,
                              BigDecimal yearsExperience,
                              Boolean verified,
                              VerificationSource verificationSource,
                              String notes) {
        if (proficiency != null) {
            this.proficiency = proficiency;
        }
        if (yearsExperience != null) {
            this.yearsExperience = yearsExperience;
        }
        if (verificationSource != null) {
            this.verificationSource = verificationSource;
        }
        if (verified != null) {
            if (verified && this.verificationSource == VerificationSource.AI_SUGGESTED) {
                // If user verifies an AI-suggested skill, elevate source to USER_EXPLICIT
                this.verificationSource = VerificationSource.USER_EXPLICIT;
            }
            this.verified = verified;
            this.lastVerifiedAt = verified ? Instant.now() : null;
        }
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSkillId() {
        return skillId;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public BigDecimal getYearsExperience() {
        return yearsExperience;
    }

    public boolean isVerified() {
        return verified;
    }

    public VerificationSource getVerificationSource() {
        return verificationSource;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getLastVerifiedAt() {
        return lastVerifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
