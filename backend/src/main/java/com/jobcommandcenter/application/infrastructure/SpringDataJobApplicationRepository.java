package com.jobcommandcenter.application.infrastructure;

import com.jobcommandcenter.application.domain.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplicationJpaEntity, UUID>, JpaSpecificationExecutor<JobApplicationJpaEntity> {

    Optional<JobApplicationJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<JobApplicationJpaEntity> findByUserIdAndJobId(UUID userId, UUID jobId);

    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT a.status, COUNT(a) FROM JobApplicationJpaEntity a WHERE a.userId = :userId GROUP BY a.status")
    List<Object[]> countByStatusForUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(a) FROM JobApplicationJpaEntity a WHERE a.userId = :userId AND a.nextFollowUpDate IS NOT NULL AND a.nextFollowUpDate <= :cutoff AND a.status NOT IN :closedStatuses")
    long countFollowUpsDueForUser(
        @Param("userId") UUID userId,
        @Param("cutoff") Instant cutoff,
        @Param("closedStatuses") Collection<ApplicationStatus> closedStatuses
    );
}
