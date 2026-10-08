package com.jobcommandcenter.job.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository {

    Optional<Job> findById(UUID id);

    Optional<Job> findBySourceAndExternalJobId(JobSource source, String externalJobId);

    Optional<Job> findByJobUrl(String jobUrl);

    Optional<Job> findByDeduplicationHash(String deduplicationHash);

    List<Job> findAll();

    List<Job> search(JobSearchCriteria criteria, int page, int size);

    long count(JobSearchCriteria criteria);

    Job save(Job job);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByDeduplicationHash(String deduplicationHash);

    boolean existsBySourceAndExternalJobId(JobSource source, String externalJobId);

    boolean existsByJobUrl(String jobUrl);
}
