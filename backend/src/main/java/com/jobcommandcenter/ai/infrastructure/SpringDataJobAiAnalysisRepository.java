package com.jobcommandcenter.ai.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataJobAiAnalysisRepository extends JpaRepository<JobAiAnalysisJpaEntity, UUID> {

    Optional<JobAiAnalysisJpaEntity> findFirstByJobIdOrderByVersionDesc(UUID jobId);

    List<JobAiAnalysisJpaEntity> findAllByJobIdOrderByVersionDesc(UUID jobId);

    @Query("SELECT COALESCE(MAX(a.version), 0) FROM JobAiAnalysisJpaEntity a WHERE a.jobId = :jobId")
    int findMaxVersionByJobId(@Param("jobId") UUID jobId);

    boolean existsByJobId(UUID jobId);
}
