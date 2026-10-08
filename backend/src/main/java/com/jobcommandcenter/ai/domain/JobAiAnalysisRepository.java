package com.jobcommandcenter.ai.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for JobAiAnalysis aggregate root.
 */
public interface JobAiAnalysisRepository {

    Optional<JobAiAnalysis> findById(UUID id);

    Optional<JobAiAnalysis> findLatestByJobId(UUID jobId);

    List<JobAiAnalysis> findAllByJobIdOrderByVersionDesc(UUID jobId);

    int findMaxVersionByJobId(UUID jobId);

    JobAiAnalysis save(JobAiAnalysis analysis);

    boolean existsByJobId(UUID jobId);
}
