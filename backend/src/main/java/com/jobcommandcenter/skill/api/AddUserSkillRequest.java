package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record AddUserSkillRequest(
    @NotNull(message = "Skill ID is required")
    UUID skillId,

    @NotNull(message = "Skill proficiency is required")
    SkillProficiency proficiency,

    @DecimalMin(value = "0.0", message = "Years of experience must be non-negative")
    BigDecimal yearsExperience,

    Boolean verified,

    VerificationSource verificationSource,

    String notes
) {
}
