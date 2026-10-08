package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.VerificationSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserSkillResponse(
    UUID id,
    UUID userId,
    UUID skillId,
    String skillName,
    SkillCategory skillCategory,
    SkillProficiency proficiency,
    BigDecimal yearsExperience,
    boolean verified,
    VerificationSource verificationSource,
    String notes,
    Instant lastVerifiedAt,
    Instant createdAt,
    Instant updatedAt
) {
    public static UserSkillResponse of(UserSkill userSkill, Skill skill) {
        return new UserSkillResponse(
            userSkill.getId(),
            userSkill.getUserId(),
            userSkill.getSkillId(),
            skill != null ? skill.getName() : "Unknown",
            skill != null ? skill.getCategory() : SkillCategory.OTHER,
            userSkill.getProficiency(),
            userSkill.getYearsExperience(),
            userSkill.isVerified(),
            userSkill.getVerificationSource(),
            userSkill.getNotes(),
            userSkill.getLastVerifiedAt(),
            userSkill.getCreatedAt(),
            userSkill.getUpdatedAt()
        );
    }
}
