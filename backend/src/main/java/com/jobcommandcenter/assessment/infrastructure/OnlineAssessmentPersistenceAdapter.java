package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class OnlineAssessmentPersistenceAdapter implements OnlineAssessmentRepository {

    private final SpringDataOnlineAssessmentRepository assessmentRepository;
    private final SpringDataAssessmentChecklistRepository checklistRepository;

    public OnlineAssessmentPersistenceAdapter(SpringDataOnlineAssessmentRepository assessmentRepository,
                                             SpringDataAssessmentChecklistRepository checklistRepository) {
        this.assessmentRepository = assessmentRepository;
        this.checklistRepository = checklistRepository;
    }

    @Override
    @Transactional
    public OnlineAssessment save(OnlineAssessment assessment) {
        OnlineAssessmentJpaEntity entity = OnlineAssessmentJpaEntity.fromDomain(assessment);
        OnlineAssessmentJpaEntity saved = assessmentRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<OnlineAssessment> findByIdAndUserId(UUID id, UUID userId) {
        return assessmentRepository.findByIdAndUserId(id, userId)
            .map(OnlineAssessmentJpaEntity::toDomain);
    }

    @Override
    public List<OnlineAssessment> findByUserId(UUID userId, OnlineAssessmentSearchCriteria criteria) {
        return assessmentRepository.searchAssessments(
            userId,
            criteria.status(),
            criteria.platform(),
            criteria.jobId(),
            criteria.applicationId(),
            criteria.expiresAfter(),
            criteria.expiresBefore()
        ).stream()
         .map(OnlineAssessmentJpaEntity::toDomain)
         .collect(Collectors.toList());
    }

    @Override
    public List<OnlineAssessment> findActiveByUserId(UUID userId) {
        return assessmentRepository.findActiveByUserId(userId).stream()
            .map(OnlineAssessmentJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<OnlineAssessment> findDueSoonByUserId(UUID userId, Instant now, Instant windowEnd) {
        return assessmentRepository.findDueSoonByUserId(userId, now, windowEnd).stream()
            .map(OnlineAssessmentJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countByUserIdAndStatus(UUID userId, AssessmentStatus status) {
        return assessmentRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public long countByUserId(UUID userId) {
        return assessmentRepository.countByUserId(userId);
    }

    @Override
    public long countDueSoonByUserId(UUID userId, Instant now, Instant windowEnd) {
        return assessmentRepository.countDueSoonByUserId(userId, now, windowEnd);
    }

    @Override
    @Transactional
    public void delete(OnlineAssessment assessment) {
        assessmentRepository.deleteById(assessment.getId());
    }

    @Override
    public Optional<AssessmentChecklistItem> findChecklistItemById(UUID assessmentId, UUID itemId) {
        return checklistRepository.findByIdAndAssessmentId(itemId, assessmentId)
            .map(AssessmentChecklistJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public AssessmentChecklistItem saveChecklistItem(AssessmentChecklistItem item) {
        AssessmentChecklistJpaEntity entity = AssessmentChecklistJpaEntity.fromDomain(item);
        AssessmentChecklistJpaEntity saved = checklistRepository.save(entity);
        return saved.toDomain();
    }
}
