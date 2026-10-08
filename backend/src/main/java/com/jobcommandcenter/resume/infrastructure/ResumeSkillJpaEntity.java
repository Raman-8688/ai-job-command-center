package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.skill.domain.SkillProficiency;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_skills", uniqueConstraints = {
    @UniqueConstraint(name = "uq_resume_skills_resume_skill", columnNames = {"resume_id", "skill_id"})
})
public class ResumeSkillJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "resume_id", insertable = false, updatable = false)
    private UUID resumeId;

    @Column(name = "skill_id", nullable = false)
    private UUID skillId;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", length = 50)
    private SkillProficiency proficiency;

    @Column(name = "years_experience", precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ResumeSkillJpaEntity() {}

    public ResumeSkillJpaEntity(UUID id,
                               UUID resumeId,
                               UUID skillId,
                               SkillProficiency proficiency,
                               BigDecimal yearsExperience,
                               Instant createdAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.skillId = skillId;
        this.proficiency = proficiency;
        this.yearsExperience = yearsExperience;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getResumeId() { return resumeId; }
    public void setResumeId(UUID resumeId) { this.resumeId = resumeId; }

    public UUID getSkillId() { return skillId; }
    public void setSkillId(UUID skillId) { this.skillId = skillId; }

    public SkillProficiency getProficiency() { return proficiency; }
    public void setProficiency(SkillProficiency proficiency) { this.proficiency = proficiency; }

    public BigDecimal getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(BigDecimal yearsExperience) { this.yearsExperience = yearsExperience; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
