package com.jobcommandcenter.interview.infrastructure;

import com.jobcommandcenter.interview.domain.InterviewRound;
import com.jobcommandcenter.interview.domain.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataInterviewRepository extends JpaRepository<InterviewJpaEntity, UUID> {

    Optional<InterviewJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT i FROM InterviewJpaEntity i WHERE i.userId = :userId " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:round IS NULL OR i.round = :round) " +
           "AND (:applicationId IS NULL OR i.applicationId = :applicationId) " +
           "AND (:jobId IS NULL OR i.jobId = :jobId) " +
           "AND (:after IS NULL OR i.scheduledStartTime >= :after) " +
           "AND (:before IS NULL OR i.scheduledStartTime <= :before) " +
           "ORDER BY i.scheduledStartTime ASC")
    List<InterviewJpaEntity> searchInterviews(
        @Param("userId") UUID userId,
        @Param("status") InterviewStatus status,
        @Param("round") InterviewRound round,
        @Param("applicationId") UUID applicationId,
        @Param("jobId") UUID jobId,
        @Param("after") Instant after,
        @Param("before") Instant before
    );

    @Query("SELECT i FROM InterviewJpaEntity i WHERE i.userId = :userId " +
           "AND i.scheduledStartTime >= :fromTime " +
           "AND i.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "ORDER BY i.scheduledStartTime ASC")
    List<InterviewJpaEntity> findUpcomingByUserId(@Param("userId") UUID userId, @Param("fromTime") Instant fromTime);

    long countByUserIdAndStatus(UUID userId, InterviewStatus status);

    long countByUserId(UUID userId);
}
