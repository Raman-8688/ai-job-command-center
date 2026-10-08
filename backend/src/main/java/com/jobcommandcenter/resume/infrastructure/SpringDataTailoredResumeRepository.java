package com.jobcommandcenter.resume.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTailoredResumeRepository extends JpaRepository<TailoredResumeJpaEntity, UUID> {

    Optional<TailoredResumeJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    List<TailoredResumeJpaEntity> findByUserIdAndSourceResumeIdOrderByVersionDesc(UUID userId, UUID sourceResumeId);

    List<TailoredResumeJpaEntity> findByUserIdAndTargetJobIdOrderByVersionDesc(UUID userId, UUID targetJobId);

    @Query("SELECT COALESCE(MAX(t.version), 0) FROM TailoredResumeJpaEntity t WHERE t.sourceResumeId = :sourceResumeId AND t.targetJobId = :targetJobId")
    int findMaxVersion(@Param("sourceResumeId") UUID sourceResumeId, @Param("targetJobId") UUID targetJobId);
}
