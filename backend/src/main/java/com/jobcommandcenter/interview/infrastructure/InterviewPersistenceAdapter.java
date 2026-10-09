package com.jobcommandcenter.interview.infrastructure;

import com.jobcommandcenter.interview.domain.Interview;
import com.jobcommandcenter.interview.domain.InterviewPreparation;
import com.jobcommandcenter.interview.domain.InterviewRepository;
import com.jobcommandcenter.interview.domain.InterviewSearchCriteria;
import com.jobcommandcenter.interview.domain.InterviewStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class InterviewPersistenceAdapter implements InterviewRepository {

    private final SpringDataInterviewRepository interviewRepository;
    private final SpringDataInterviewPreparationRepository preparationRepository;

    public InterviewPersistenceAdapter(SpringDataInterviewRepository interviewRepository,
                                       SpringDataInterviewPreparationRepository preparationRepository) {
        this.interviewRepository = interviewRepository;
        this.preparationRepository = preparationRepository;
    }

    @Override
    public Interview save(Interview interview) {
        InterviewJpaEntity entity = InterviewJpaEntity.fromDomain(interview);
        InterviewJpaEntity saved = interviewRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Interview> findByIdAndUserId(UUID id, UUID userId) {
        return interviewRepository.findByIdAndUserId(id, userId)
            .map(InterviewJpaEntity::toDomain);
    }

    @Override
    public List<Interview> findByUserId(UUID userId, InterviewSearchCriteria criteria) {
        return interviewRepository.searchInterviews(
            userId,
            criteria.status(),
            criteria.round(),
            criteria.applicationId(),
            criteria.jobId(),
            criteria.scheduledAfter(),
            criteria.scheduledBefore()
        ).stream()
         .map(InterviewJpaEntity::toDomain)
         .collect(Collectors.toList());
    }

    @Override
    public List<Interview> findUpcomingByUserId(UUID userId, Instant fromTime) {
        return interviewRepository.findUpcomingByUserId(userId, fromTime).stream()
            .map(InterviewJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countByUserIdAndStatus(UUID userId, InterviewStatus status) {
        return interviewRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public long countByUserId(UUID userId) {
        return interviewRepository.countByUserId(userId);
    }

    @Override
    public void delete(Interview interview) {
        interviewRepository.deleteById(interview.getId());
    }

    @Override
    public Optional<InterviewPreparation> findPreparationById(UUID interviewId, UUID prepId) {
        return preparationRepository.findByIdAndInterviewId(prepId, interviewId)
            .map(InterviewPreparationJpaEntity::toDomain);
    }

    @Override
    public InterviewPreparation savePreparation(InterviewPreparation preparation) {
        InterviewPreparationJpaEntity entity = InterviewPreparationJpaEntity.fromDomain(preparation);
        InterviewPreparationJpaEntity saved = preparationRepository.save(entity);
        return saved.toDomain();
    }
}
