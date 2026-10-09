package com.jobcommandcenter.application.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataJobApplicationEventRepository extends JpaRepository<JobApplicationEventJpaEntity, UUID> {

    List<JobApplicationEventJpaEntity> findByApplicationIdOrderByOccurredAtAsc(UUID applicationId);
}
