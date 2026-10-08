package com.jobcommandcenter.job.api;

import com.jobcommandcenter.job.domain.SkillRequirementType;

import java.math.BigDecimal;
import java.util.UUID;

public record JobSkillDto(
    UUID skillId,
    String skillName,
    SkillRequirementType requirementType,
    BigDecimal yearsExperienceRequired
) {}
