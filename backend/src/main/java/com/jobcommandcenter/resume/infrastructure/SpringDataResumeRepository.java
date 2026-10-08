package com.jobcommandcenter.resume.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataResumeRepository extends JpaRepository<ResumeJpaEntity, UUID> {

    List<ResumeJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<ResumeJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
