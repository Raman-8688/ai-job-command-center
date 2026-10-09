package com.jobcommandcenter.assessment.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for managing OnlineAssessment aggregates.
 */
public interface OnlineAssessmentRepository {

    OnlineAssessment save(OnlineAssessment assessment);

    Optional<OnlineAssessment> findByIdAndUserId(UUID id, UUID userId);

    List<OnlineAssessment> findByUserId(UUID userId, OnlineAssessmentSearchCriteria criteria);

    List<OnlineAssessment> findActiveByUserId(UUID userId);

    List<OnlineAssessment> findDueSoonByUserId(UUID userId, Instant now, Instant windowEnd);

    long countByUserIdAndStatus(UUID userId, AssessmentStatus status);

    long countByUserId(UUID userId);

    long countDueSoonByUserId(UUID userId, Instant now, Instant windowEnd);

    void delete(OnlineAssessment assessment);

    Optional<AssessmentChecklistItem> findChecklistItemById(UUID assessmentId, UUID itemId);

    AssessmentChecklistItem saveChecklistItem(AssessmentChecklistItem item);
}
