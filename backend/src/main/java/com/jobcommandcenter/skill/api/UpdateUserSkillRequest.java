package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.domain.SkillProficiency;
import com.jobcommandcenter.skill.domain.VerificationSource;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateUserSkillRequest(
    SkillProficiency proficiency,

    @DecimalMin(value = "0.0", message = "Years of experience must be non-negative")
    BigDecimal yearsExperience,

    Boolean verified,

    VerificationSource verificationSource,

    String notes
) {
}
