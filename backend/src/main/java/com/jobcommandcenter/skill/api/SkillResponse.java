package com.jobcommandcenter.skill.api;

import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;

import java.time.Instant;
import java.util.UUID;

public record SkillResponse(
    UUID id,
    String name,
    String normalizedName,
    SkillCategory category,
    Instant createdAt
) {
    public static SkillResponse fromDomain(Skill skill) {
        return new SkillResponse(
            skill.getId(),
            skill.getName(),
            skill.getNormalizedName(),
            skill.getCategory(),
            skill.getCreatedAt()
        );
    }
}
