package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.AssessmentPlatform;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataOnlineAssessmentRepository extends JpaRepository<OnlineAssessmentJpaEntity, UUID> {

    Optional<OnlineAssessmentJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT a FROM OnlineAssessmentJpaEntity a WHERE a.userId = :userId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:platform IS NULL OR a.platform = :platform) " +
           "AND (:jobId IS NULL OR a.jobId = :jobId) " +
           "AND (:applicationId IS NULL OR a.applicationId = :applicationId) " +
           "AND (:expiresAfter IS NULL OR a.expiresAt >= :expiresAfter) " +
           "AND (:expiresBefore IS NULL OR a.expiresAt <= :expiresBefore) " +
           "ORDER BY a.expiresAt ASC NULLS LAST, a.createdAt DESC")
    List<OnlineAssessmentJpaEntity> searchAssessments(
        @Param("userId") UUID userId,
        @Param("status") AssessmentStatus status,
        @Param("platform") AssessmentPlatform platform,
        @Param("jobId") UUID jobId,
        @Param("applicationId") UUID applicationId,
        @Param("expiresAfter") Instant expiresAfter,
        @Param("expiresBefore") Instant expiresBefore
    );

    @Query("SELECT a FROM OnlineAssessmentJpaEntity a WHERE a.userId = :userId " +
           "AND a.status IN ('INVITED', 'IN_PROGRESS') " +
           "ORDER BY a.expiresAt ASC NULLS LAST, a.createdAt DESC")
    List<OnlineAssessmentJpaEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM OnlineAssessmentJpaEntity a WHERE a.userId = :userId " +
           "AND a.status IN ('INVITED', 'IN_PROGRESS') " +
           "AND a.expiresAt IS NOT NULL " +
           "AND a.expiresAt >= :now AND a.expiresAt <= :windowEnd " +
           "ORDER BY a.expiresAt ASC")
    List<OnlineAssessmentJpaEntity> findDueSoonByUserId(
        @Param("userId") UUID userId,
        @Param("now") Instant now,
        @Param("windowEnd") Instant windowEnd
    );

    long countByUserIdAndStatus(UUID userId, AssessmentStatus status);

    long countByUserId(UUID userId);

    @Query("SELECT count(a) FROM OnlineAssessmentJpaEntity a WHERE a.userId = :userId " +
           "AND a.status IN ('INVITED', 'IN_PROGRESS') " +
           "AND a.expiresAt IS NOT NULL " +
           "AND a.expiresAt >= :now AND a.expiresAt <= :windowEnd")
    long countDueSoonByUserId(
        @Param("userId") UUID userId,
        @Param("now") Instant now,
        @Param("windowEnd") Instant windowEnd
    );
}
