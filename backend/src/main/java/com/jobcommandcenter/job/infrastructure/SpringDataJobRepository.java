package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataJobRepository extends JpaRepository<JobJpaEntity, UUID>, JpaSpecificationExecutor<JobJpaEntity> {

    Optional<JobJpaEntity> findBySourceAndExternalJobId(JobSource source, String externalJobId);

    Optional<JobJpaEntity> findByJobUrl(String jobUrl);

    Optional<JobJpaEntity> findByDeduplicationHash(String deduplicationHash);

    boolean existsByDeduplicationHash(String deduplicationHash);

    boolean existsBySourceAndExternalJobId(JobSource source, String externalJobId);

    boolean existsByJobUrl(String jobUrl);
}
