package com.jobcommandcenter.job.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserJobRepository extends JpaRepository<UserJobJpaEntity, UUID> {

    Optional<UserJobJpaEntity> findByUserIdAndJobId(UUID userId, UUID jobId);

    List<UserJobJpaEntity> findByUserId(UUID userId);

    void deleteByUserIdAndJobId(UUID userId, UUID jobId);

    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);
}
