package com.jobcommandcenter.job.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJobRepository {

    Optional<UserJob> findByUserIdAndJobId(UUID userId, UUID jobId);

    List<UserJob> findByUserId(UUID userId);

    UserJob save(UserJob userJob);

    void deleteByUserIdAndJobId(UUID userId, UUID jobId);

    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);
}
