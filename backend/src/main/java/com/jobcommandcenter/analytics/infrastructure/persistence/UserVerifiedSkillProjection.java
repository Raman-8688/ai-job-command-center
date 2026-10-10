package com.jobcommandcenter.analytics.infrastructure.persistence;

import java.util.UUID;

/**
 * Typed projection for candidate verified skills.
 */
public record UserVerifiedSkillProjection(
    UUID skillId,
    String skillName,
    boolean verified,
    String proficiency
) {}
