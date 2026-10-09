package com.jobcommandcenter.interview.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Filter criteria for querying user interviews.
 */
public record InterviewSearchCriteria(
    InterviewStatus status,
    InterviewRound round,
    UUID applicationId,
    UUID jobId,
    Instant scheduledAfter,
    Instant scheduledBefore
) {
    public static InterviewSearchCriteria empty() {
        return new InterviewSearchCriteria(null, null, null, null, null, null);
    }
}
