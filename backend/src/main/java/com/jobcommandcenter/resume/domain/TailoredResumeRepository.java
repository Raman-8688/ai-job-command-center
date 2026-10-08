package com.jobcommandcenter.resume.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for TailoredResume aggregate root.
 */
public interface TailoredResumeRepository {

    TailoredResume save(TailoredResume tailoredResume);

    Optional<TailoredResume> findById(UUID id);

    Optional<TailoredResume> findByIdAndUserId(UUID id, UUID userId);

    List<TailoredResume> findByUserIdAndSourceResumeId(UUID userId, UUID sourceResumeId);

    List<TailoredResume> findByUserIdAndTargetJobId(UUID userId, UUID targetJobId);

    int findMaxVersion(UUID sourceResumeId, UUID targetJobId);

    void deleteById(UUID id);
}
