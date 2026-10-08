package com.jobcommandcenter.resume.domain;

import com.jobcommandcenter.skill.domain.SkillProficiency;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a skill listed on a specific resume.
 * Links to the canonical Skill catalog.
 */
public class ResumeSkill {

    private final UUID id;
    private final UUID skillId;
    private String skillName;
    private SkillProficiency proficiency;
    private BigDecimal yearsExperience;

    public ResumeSkill(UUID id,
                       UUID skillId,
                       String skillName,
                       SkillProficiency proficiency,
                       BigDecimal yearsExperience) {
        this.id = Objects.requireNonNull(id, "ResumeSkill ID cannot be null");
        this.skillId = Objects.requireNonNull(skillId, "Skill ID cannot be null");
        this.skillName = skillName;
        this.proficiency = proficiency != null ? proficiency : SkillProficiency.INTERMEDIATE;
        this.yearsExperience = yearsExperience != null ? yearsExperience : BigDecimal.ZERO;
    }

    public static ResumeSkill create(UUID skillId,
                                     String skillName,
                                     SkillProficiency proficiency,
                                     BigDecimal yearsExperience) {
        return new ResumeSkill(
            UUID.randomUUID(),
            skillId,
            skillName,
            proficiency,
            yearsExperience
        );
    }

    public UUID getId() { return id; }
    public UUID getSkillId() { return skillId; }
    public String getSkillName() { return skillName; }
    public SkillProficiency getProficiency() { return proficiency; }
    public BigDecimal getYearsExperience() { return yearsExperience; }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public void update(SkillProficiency proficiency, BigDecimal yearsExperience) {
        if (proficiency != null) {
            this.proficiency = proficiency;
        }
        if (yearsExperience != null) {
            this.yearsExperience = yearsExperience;
        }
    }
}
