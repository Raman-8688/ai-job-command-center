package com.jobcommandcenter.resume.api.dto;

import java.math.BigDecimal;

public record ExperienceAlignmentDto(
    BigDecimal candidateYearsExperience,
    BigDecimal requiredMinYears,
    BigDecimal requiredMaxYears,
    boolean meetsExperienceRequirement,
    String alignmentNotes
) {}
