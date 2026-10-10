package com.jobcommandcenter.analytics.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

/**
 * Typed projection for application status transition events.
 */
public record StatusTransitionProjection(
    UUID applicationId,
    String previousStatus,
    String newStatus,
    Instant occurredAt
) {}
