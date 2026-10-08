package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.UserJob;
import com.jobcommandcenter.job.domain.UserJobRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserJobPersistenceAdapter implements UserJobRepository {

    private final SpringDataUserJobRepository springDataUserJobRepository;

    public UserJobPersistenceAdapter(SpringDataUserJobRepository springDataUserJobRepository) {
        this.springDataUserJobRepository = springDataUserJobRepository;
    }

    @Override
    public Optional<UserJob> findByUserIdAndJobId(UUID userId, UUID jobId) {
        return springDataUserJobRepository.findByUserIdAndJobId(userId, jobId).map(this::toDomain);
    }

    @Override
    public List<UserJob> findByUserId(UUID userId) {
        return springDataUserJobRepository.findByUserId(userId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public UserJob save(UserJob userJob) {
        UserJobJpaEntity entity = toEntity(userJob);
        UserJobJpaEntity saved = springDataUserJobRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByUserIdAndJobId(UUID userId, UUID jobId) {
        springDataUserJobRepository.deleteByUserIdAndJobId(userId, jobId);
    }

    @Override
    public boolean existsByUserIdAndJobId(UUID userId, UUID jobId) {
        return springDataUserJobRepository.existsByUserIdAndJobId(userId, jobId);
    }

    private UserJob toDomain(UserJobJpaEntity entity) {
        return new UserJob(
            entity.getId(),
            entity.getUserId(),
            entity.getJobId(),
            entity.getStatus(),
            entity.getNotes(),
            entity.getDiscoveredAt(),
            entity.getUpdatedAt()
        );
    }

    private UserJobJpaEntity toEntity(UserJob userJob) {
        return new UserJobJpaEntity(
            userJob.getId(),
            userJob.getUserId(),
            userJob.getJobId(),
            userJob.getStatus(),
            userJob.getNotes(),
            userJob.getDiscoveredAt(),
            userJob.getUpdatedAt()
        );
    }
}
