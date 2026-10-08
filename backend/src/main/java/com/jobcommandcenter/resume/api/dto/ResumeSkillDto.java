package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.skill.domain.SkillProficiency;
import java.math.BigDecimal;
import java.util.UUID;

public record ResumeSkillDto(
    UUID id,
    UUID skillId,
    String skillName,
    SkillProficiency proficiency,
    BigDecimal yearsExperience
) {}
