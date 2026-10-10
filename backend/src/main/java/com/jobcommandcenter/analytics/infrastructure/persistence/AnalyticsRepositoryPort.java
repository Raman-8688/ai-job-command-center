package com.jobcommandcenter.analytics.infrastructure.persistence;

import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.interview.domain.InterviewOutcome;
import com.jobcommandcenter.interview.domain.InterviewRound;

import java.util.List;
import java.util.UUID;

/**
 * Persistence port contract for candidate analytics.
 * Strictly enforces tenant isolation via parameterized userId in every query.
 */
public interface AnalyticsRepositoryPort {

    /**
     * Counts applications grouped by status for the authenticated user.
     */
    List<Object[]> countApplicationsByStatus(UUID userId);

    /**
     * Retrieves all status change audit events for the authenticated user's applications, ordered by occurredAt.
     */
    List<StatusTransitionProjection> findApplicationTransitions(UUID userId);

    /**
     * Retrieves applications summary grouped by submission source and status for the user.
     */
    List<Object[]> countApplicationsBySourceAndStatus(UUID userId);

    /**
     * Counts interviews for user by round and outcome.
     */
    List<Object[]> countInterviewsByRoundAndOutcome(UUID userId);

    /**
     * Counts online assessments for user by status and result.
     */
    List<Object[]> countAssessmentsByStatusAndResult(UUID userId);

    /**
     * Counts distinct targeted jobs for the user across saved/applied pipelines.
     */
    long countDistinctTargetJobs(UUID userId);

    /**
     * Retrieves canonical skill requirements across the user's targeted jobs.
     */
    List<JobSkillRequirementProjection> findTargetJobSkillRequirements(UUID userId);

    /**
     * Retrieves all candidate skills and their verification status.
     */
    List<UserVerifiedSkillProjection> findCandidateSkills(UUID userId);
}
