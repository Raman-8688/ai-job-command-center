package com.jobcommandcenter.analytics.infrastructure.persistence;

import java.util.UUID;

/**
 * Typed projection for target job skill requirements.
 */
public record JobSkillRequirementProjection(
    UUID skillId,
    String skillName,
    String category,
    UUID jobId
) {}
