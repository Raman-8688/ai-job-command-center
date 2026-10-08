package com.jobcommandcenter.resume.domain;

import java.math.BigDecimal;

/**
 * Value object comparing candidate years of experience against job posting expectations.
 */
public record ExperienceAlignment(
    BigDecimal candidateYearsExperience,
    BigDecimal requiredMinYears,
    BigDecimal requiredMaxYears,
    boolean meetsExperienceRequirement,
    String alignmentNotes
) {}
