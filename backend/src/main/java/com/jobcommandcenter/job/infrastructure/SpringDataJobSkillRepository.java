package com.jobcommandcenter.job.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataJobSkillRepository extends JpaRepository<JobSkillJpaEntity, UUID> {

    List<JobSkillJpaEntity> findByJobId(UUID jobId);

    void deleteByJobId(UUID jobId);
}
