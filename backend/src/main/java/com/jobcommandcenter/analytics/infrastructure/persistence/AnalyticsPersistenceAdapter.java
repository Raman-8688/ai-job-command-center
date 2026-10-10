package com.jobcommandcenter.analytics.infrastructure.persistence;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Persistence adapter implementation delegating to SpringDataAnalyticsRepository.
 * Ensures read-only tenant isolation.
 */
@Component
public class AnalyticsPersistenceAdapter implements AnalyticsRepositoryPort {

    private final SpringDataAnalyticsRepository analyticsRepository;

    public AnalyticsPersistenceAdapter(SpringDataAnalyticsRepository analyticsRepository) {
        this.analyticsRepository = Objects.requireNonNull(analyticsRepository, "analyticsRepository must not be null");
    }

    @Override
    public List<Object[]> countApplicationsByStatus(UUID userId) {
        return analyticsRepository.countApplicationsByStatus(userId);
    }

    @Override
    public List<StatusTransitionProjection> findApplicationTransitions(UUID userId) {
        return analyticsRepository.findApplicationTransitions(userId);
    }

    @Override
    public List<Object[]> countApplicationsBySourceAndStatus(UUID userId) {
        return analyticsRepository.countApplicationsBySourceAndStatus(userId);
    }

    @Override
    public List<Object[]> countInterviewsByRoundAndOutcome(UUID userId) {
        return analyticsRepository.countInterviewsByRoundAndOutcome(userId);
    }

    @Override
    public List<Object[]> countAssessmentsByStatusAndResult(UUID userId) {
        return analyticsRepository.countAssessmentsByStatusAndResult(userId);
    }

    @Override
    public long countDistinctTargetJobs(UUID userId) {
        return analyticsRepository.countDistinctTargetJobs(userId);
    }

    @Override
    public List<JobSkillRequirementProjection> findTargetJobSkillRequirements(UUID userId) {
        return analyticsRepository.findTargetJobSkillRequirements(userId);
    }

    @Override
    public List<UserVerifiedSkillProjection> findCandidateSkills(UUID userId) {
        return analyticsRepository.findCandidateSkills(userId);
    }
}
