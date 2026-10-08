package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.domain.SkillCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSkillRequest(
    @NotBlank(message = "Skill name cannot be blank")
    @Size(max = 100, message = "Skill name must not exceed 100 characters")
    String name,

    @NotNull(message = "Skill category cannot be null")
    SkillCategory category
) {
}
