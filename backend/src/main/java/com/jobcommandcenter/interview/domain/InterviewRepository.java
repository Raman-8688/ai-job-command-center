package com.jobcommandcenter.interview.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for managing Interview aggregates.
 */
public interface InterviewRepository {

    Interview save(Interview interview);

    Optional<Interview> findByIdAndUserId(UUID id, UUID userId);

    List<Interview> findByUserId(UUID userId, InterviewSearchCriteria criteria);

    List<Interview> findUpcomingByUserId(UUID userId, Instant fromTime);

    long countByUserIdAndStatus(UUID userId, InterviewStatus status);

    long countByUserId(UUID userId);

    void delete(Interview interview);

    Optional<InterviewPreparation> findPreparationById(UUID interviewId, UUID prepId);

    InterviewPreparation savePreparation(InterviewPreparation preparation);
}
