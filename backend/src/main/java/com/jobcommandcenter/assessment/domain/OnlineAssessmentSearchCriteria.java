package com.jobcommandcenter.assessment.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Filter criteria for querying online assessments.
 */
public record OnlineAssessmentSearchCriteria(
    AssessmentStatus status,
    AssessmentPlatform platform,
    UUID jobId,
    UUID applicationId,
    Instant expiresBefore,
    Instant expiresAfter
) {
    public static OnlineAssessmentSearchCriteria empty() {
        return new OnlineAssessmentSearchCriteria(null, null, null, null, null, null);
    }
}
