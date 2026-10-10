package com.jobcommandcenter.analytics.infrastructure.persistence;

import com.jobcommandcenter.application.infrastructure.JobApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository providing aggregate queries for candidate analytics.
 * Strictly enforces tenant isolation via parameterized :userId.
 */
@Repository
public interface SpringDataAnalyticsRepository extends JpaRepository<JobApplicationJpaEntity, UUID> {

    @Query("SELECT a.status, COUNT(a) FROM JobApplicationJpaEntity a " +
           "WHERE a.userId = :userId GROUP BY a.status")
    List<Object[]> countApplicationsByStatus(@Param("userId") UUID userId);

    @Query("SELECT new com.jobcommandcenter.analytics.infrastructure.persistence.StatusTransitionProjection(" +
           "e.applicationId, CAST(e.previousStatus AS string), CAST(e.newStatus AS string), e.occurredAt) " +
           "FROM JobApplicationJpaEntity a " +
           "JOIN JobApplicationEventJpaEntity e ON e.applicationId = a.id " +
           "WHERE a.userId = :userId " +
           "ORDER BY e.applicationId, e.occurredAt ASC")
    List<StatusTransitionProjection> findApplicationTransitions(@Param("userId") UUID userId);

    @Query("SELECT a.submissionSource, a.status, COUNT(a) " +
           "FROM JobApplicationJpaEntity a " +
           "WHERE a.userId = :userId " +
           "GROUP BY a.submissionSource, a.status")
    List<Object[]> countApplicationsBySourceAndStatus(@Param("userId") UUID userId);

    @Query("SELECT i.round, i.outcome, COUNT(i) " +
           "FROM InterviewJpaEntity i " +
           "WHERE i.userId = :userId " +
           "GROUP BY i.round, i.outcome")
    List<Object[]> countInterviewsByRoundAndOutcome(@Param("userId") UUID userId);

    @Query("SELECT o.status, o.result, COUNT(o) " +
           "FROM OnlineAssessmentJpaEntity o " +
           "WHERE o.userId = :userId " +
           "GROUP BY o.status, o.result")
    List<Object[]> countAssessmentsByStatusAndResult(@Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT j.id) " +
           "FROM JobJpaEntity j " +
           "WHERE j.id IN (" +
           "  SELECT a.jobId FROM JobApplicationJpaEntity a WHERE a.userId = :userId" +
           ") OR j.id IN (" +
           "  SELECT uj.jobId FROM UserJobJpaEntity uj WHERE uj.userId = :userId" +
           ")")
    long countDistinctTargetJobs(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT new com.jobcommandcenter.analytics.infrastructure.persistence.JobSkillRequirementProjection(" +
           "s.id, s.name, CAST(s.category AS string), js.jobId) " +
           "FROM JobSkillJpaEntity js " +
           "JOIN SkillJpaEntity s ON s.id = js.skillId " +
           "WHERE js.jobId IN (" +
           "  SELECT a.jobId FROM JobApplicationJpaEntity a WHERE a.userId = :userId" +
           ") OR js.jobId IN (" +
           "  SELECT uj.jobId FROM UserJobJpaEntity uj WHERE uj.userId = :userId" +
           ")")
    List<JobSkillRequirementProjection> findTargetJobSkillRequirements(@Param("userId") UUID userId);

    @Query("SELECT new com.jobcommandcenter.analytics.infrastructure.persistence.UserVerifiedSkillProjection(" +
           "s.id, s.name, us.verified, CAST(us.proficiency AS string)) " +
           "FROM UserSkillJpaEntity us " +
           "JOIN SkillJpaEntity s ON s.id = us.skillId " +
           "WHERE us.userId = :userId")
    List<UserVerifiedSkillProjection> findCandidateSkills(@Param("userId") UUID userId);
}
