package com.jobcommandcenter.application.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for JobApplication persistence and querying.
 */
public interface JobApplicationRepository {

    Optional<JobApplication> findById(UUID id);

    Optional<JobApplication> findByUserIdAndJobId(UUID userId, UUID jobId);

    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);

    List<JobApplication> search(JobApplicationSearchCriteria criteria);

    long count(JobApplicationSearchCriteria criteria);

    JobApplication save(JobApplication application);

    void delete(JobApplication application);

    Map<ApplicationStatus, Long> countByStatusForUser(UUID userId);

    long countFollowUpsDueForUser(UUID userId);
}
