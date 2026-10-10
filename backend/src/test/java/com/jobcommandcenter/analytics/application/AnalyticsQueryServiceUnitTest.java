package com.jobcommandcenter.analytics.application;

import com.jobcommandcenter.analytics.domain.*;
import com.jobcommandcenter.analytics.infrastructure.persistence.*;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import com.jobcommandcenter.interview.domain.InterviewOutcome;
import com.jobcommandcenter.interview.domain.InterviewRound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsQueryService Unit Tests")
class AnalyticsQueryServiceUnitTest {

    @Mock
    private AnalyticsRepositoryPort analyticsRepository;

    private AnalyticsQueryService analyticsService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsQueryService(analyticsRepository);
    }

    @Test
    @DisplayName("Returns safe zeroed metrics for empty candidate profile without exceptions")
    void shouldReturnSafeZeroedMetricsForEmptyCandidate() {
        when(analyticsRepository.countApplicationsByStatus(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsRepository.countInterviewsByRoundAndOutcome(eq(userId))).thenReturn(Collections.emptyList());
        when(analyticsRepository.countAssessmentsByStatusAndResult(eq(userId))).thenReturn(Collections.emptyList());

        AnalyticsOverview overview = analyticsService.getOverview(userId);

        assertThat(overview.totalApplications()).isEqualTo(0);
        assertThat(overview.activePipelines()).isEqualTo(0);
        assertThat(overview.interviewsCount()).isEqualTo(0);
        assertThat(overview.assessmentsCount()).isEqualTo(0);
        assertThat(overview.activeOffers()).isEqualTo(0);
        assertThat(overview.rejectionsCount()).isEqualTo(0);
        assertThat(overview.interviewConversionRatePercent()).isEqualTo(new BigDecimal("0.00"));
        assertThat(overview.assessmentPassRatePercent()).isEqualTo(new BigDecimal("0.00"));
        assertThat(overview.offerRatePercent()).isEqualTo(new BigDecimal("0.00"));

        FunnelMetrics funnel = analyticsService.getFunnelMetrics(userId);
        assertThat(funnel.totalApplications()).isEqualTo(0);
        assertThat(funnel.stageConversions()).isEmpty();

        List<SourceEffectiveness> sources = analyticsService.getSourceEffectiveness(userId);
        assertThat(sources).isEmpty();

        when(analyticsRepository.countDistinctTargetJobs(eq(userId))).thenReturn(0L);
        List<SkillGapMetric> skillGaps = analyticsService.getSkillGaps(userId);
        assertThat(skillGaps).isEmpty();
    }

    @Test
    @DisplayName("Computes overview KPIs accurately with evaluated assessment pass rate")
    void shouldComputeOverviewKpisAccurately() {
        // 100 applications: 10 DRAFT, 40 APPLIED, 20 SCREENING, 10 ASSESSMENT, 10 INTERVIEW, 5 OFFER, 5 REJECTED
        List<Object[]> statusRows = List.of(
            new Object[]{ApplicationStatus.DRAFT, 10L},
            new Object[]{ApplicationStatus.APPLIED, 40L},
            new Object[]{ApplicationStatus.SCREENING, 20L},
            new Object[]{ApplicationStatus.ASSESSMENT, 10L},
            new Object[]{ApplicationStatus.INTERVIEW, 10L},
            new Object[]{ApplicationStatus.OFFER, 5L},
            new Object[]{ApplicationStatus.REJECTED, 5L}
        );
        when(analyticsRepository.countApplicationsByStatus(eq(userId))).thenReturn(statusRows);

        // 15 interviews total
        List<Object[]> interviewRows = List.of(
            new Object[]{InterviewRound.TECHNICAL_SCREEN, InterviewOutcome.PASSED, 10L},
            new Object[]{InterviewRound.SYSTEM_DESIGN, InterviewOutcome.PENDING, 5L}
        );
        when(analyticsRepository.countInterviewsByRoundAndOutcome(eq(userId))).thenReturn(interviewRows);

        // Assessments: 5 passed, 3 failed, 2 pending
        List<Object[]> assessmentRows = List.of(
            new Object[]{AssessmentStatus.SUBMITTED, AssessmentResult.PASSED, 5L},
            new Object[]{AssessmentStatus.SUBMITTED, AssessmentResult.FAILED, 3L},
            new Object[]{AssessmentStatus.IN_PROGRESS, AssessmentResult.PENDING, 2L}
        );
        when(analyticsRepository.countAssessmentsByStatusAndResult(eq(userId))).thenReturn(assessmentRows);

        AnalyticsOverview overview = analyticsService.getOverview(userId);

        assertThat(overview.totalApplications()).isEqualTo(100L);
        // Active = 40 (APPLIED) + 20 (SCREENING) + 10 (ASSESSMENT) + 10 (INTERVIEW) = 80
        assertThat(overview.activePipelines()).isEqualTo(80L);
        assertThat(overview.interviewsCount()).isEqualTo(15L);
        assertThat(overview.assessmentsCount()).isEqualTo(10L);
        assertThat(overview.activeOffers()).isEqualTo(5L);
        assertThat(overview.rejectionsCount()).isEqualTo(5L);

        // Interview rate: 10 (INTERVIEW) + 5 (OFFER) / 100 = 15.00%
        assertThat(overview.interviewConversionRatePercent()).isEqualTo(new BigDecimal("15.00"));

        // Assessment pass rate: 5 passed / (5 passed + 3 failed = 8 evaluated) = 62.50%
        assertThat(overview.assessmentPassRatePercent()).isEqualTo(new BigDecimal("62.50"));

        // Offer rate: 5 offers / 100 = 5.00%
        assertThat(overview.offerRatePercent()).isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Computes funnel transitions and median duration correctly from audit events")
    void shouldComputeFunnelTransitionsAndMedianDuration() {
        List<Object[]> statusRows = List.of(
            new Object[]{ApplicationStatus.APPLIED, 1L},
            new Object[]{ApplicationStatus.SCREENING, 1L},
            new Object[]{ApplicationStatus.INTERVIEW, 1L},
            new Object[]{ApplicationStatus.OFFER, 1L}
        );
        when(analyticsRepository.countApplicationsByStatus(eq(userId))).thenReturn(statusRows);

        UUID app1 = UUID.randomUUID();
        UUID app2 = UUID.randomUUID();
        Instant now = Instant.now();

        // app1: APPLIED -> SCREENING (after 2 days) -> INTERVIEW (after 3 days) -> OFFER (after 4 days)
        // app2: APPLIED -> SCREENING (after 4 days)
        List<StatusTransitionProjection> transitions = List.of(
            new StatusTransitionProjection(app1, null, "APPLIED", now.minus(10, ChronoUnit.DAYS)),
            new StatusTransitionProjection(app1, "APPLIED", "SCREENING", now.minus(8, ChronoUnit.DAYS)), // 2 days in APPLIED
            new StatusTransitionProjection(app1, "SCREENING", "INTERVIEW", now.minus(5, ChronoUnit.DAYS)), // 3 days in SCREENING
            new StatusTransitionProjection(app1, "INTERVIEW", "OFFER", now.minus(1, ChronoUnit.DAYS)), // 4 days in INTERVIEW

            new StatusTransitionProjection(app2, null, "APPLIED", now.minus(6, ChronoUnit.DAYS)),
            new StatusTransitionProjection(app2, "APPLIED", "SCREENING", now.minus(2, ChronoUnit.DAYS)) // 4 days in APPLIED
        );
        when(analyticsRepository.findApplicationTransitions(eq(userId))).thenReturn(transitions);

        FunnelMetrics funnel = analyticsService.getFunnelMetrics(userId);

        assertThat(funnel.totalApplications()).isEqualTo(4L);
        assertThat(funnel.stageConversions()).hasSize(4);

        // APPLIED -> SCREENING median days = (2.0 + 4.0)/2 = 3.0 days
        StageConversionRate appliedToScreening = funnel.stageConversions().get(0);
        assertThat(appliedToScreening.fromStage()).isEqualTo("APPLIED");
        assertThat(appliedToScreening.toStage()).isEqualTo("SCREENING");
        assertThat(appliedToScreening.medianDaysInStage()).isEqualTo(3.0);

        // SCREENING -> INTERVIEW median days = 3.0 days
        StageConversionRate screeningToInterview = funnel.stageConversions().get(1);
        assertThat(screeningToInterview.fromStage()).isEqualTo("SCREENING");
        assertThat(screeningToInterview.toStage()).isEqualTo("INTERVIEW");
        assertThat(screeningToInterview.medianDaysInStage()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Computes source effectiveness metrics accurately and groups null sources as MANUAL")
    void shouldComputeSourceEffectivenessAccurately() {
        List<Object[]> sourceRows = List.of(
            new Object[]{ApplicationSource.LINKEDIN, ApplicationStatus.APPLIED, 10L},
            new Object[]{ApplicationSource.LINKEDIN, ApplicationStatus.SCREENING, 5L},
            new Object[]{ApplicationSource.LINKEDIN, ApplicationStatus.INTERVIEW, 3L},
            new Object[]{ApplicationSource.LINKEDIN, ApplicationStatus.OFFER, 2L}, // total 20

            new Object[]{null, ApplicationStatus.APPLIED, 5L}, // null grouped as MANUAL
            new Object[]{ApplicationSource.MANUAL, ApplicationStatus.INTERVIEW, 5L} // total MANUAL = 10
        );
        when(analyticsRepository.countApplicationsBySourceAndStatus(eq(userId))).thenReturn(sourceRows);

        List<SourceEffectiveness> sources = analyticsService.getSourceEffectiveness(userId);

        assertThat(sources).hasSize(2);

        SourceEffectiveness linkedin = sources.stream().filter(s -> s.source() == ApplicationSource.LINKEDIN).findFirst().orElseThrow();
        assertThat(linkedin.totalApplications()).isEqualTo(20L);
        // Screenings reached: 5 + 3 + 2 = 10 -> 50.00%
        assertThat(linkedin.screeningRatePercent()).isEqualTo(new BigDecimal("50.00"));
        // Interviews reached: 3 + 2 = 5 -> 25.00%
        assertThat(linkedin.interviewRatePercent()).isEqualTo(new BigDecimal("25.00"));
        // Offers received: 2 -> 10.00%
        assertThat(linkedin.offerRatePercent()).isEqualTo(new BigDecimal("10.00"));

        SourceEffectiveness manual = sources.stream().filter(s -> s.source() == ApplicationSource.MANUAL).findFirst().orElseThrow();
        assertThat(manual.totalApplications()).isEqualTo(10L);
        assertThat(manual.interviewRatePercent()).isEqualTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Identifies skill gaps prioritizing unverified skills by market demand percentage")
    void shouldIdentifySkillGapsAccurately() {
        when(analyticsRepository.countDistinctTargetJobs(eq(userId))).thenReturn(10L);

        UUID skillJava = UUID.randomUUID();
        UUID skillK8s = UUID.randomUUID();
        UUID skillPython = UUID.randomUUID();

        UUID job1 = UUID.randomUUID();
        UUID job2 = UUID.randomUUID();
        UUID job3 = UUID.randomUUID();

        List<JobSkillRequirementProjection> jobSkills = List.of(
            // Java required in 3 jobs
            new JobSkillRequirementProjection(skillJava, "Java", "BACKEND", job1),
            new JobSkillRequirementProjection(skillJava, "Java", "BACKEND", job2),
            new JobSkillRequirementProjection(skillJava, "Java", "BACKEND", job3),

            // Kubernetes required in 2 jobs
            new JobSkillRequirementProjection(skillK8s, "Kubernetes", "DEVOPS", job1),
            new JobSkillRequirementProjection(skillK8s, "Kubernetes", "DEVOPS", job2),

            // Python required in 1 job
            new JobSkillRequirementProjection(skillPython, "Python", "BACKEND", job1)
        );
        when(analyticsRepository.findTargetJobSkillRequirements(eq(userId))).thenReturn(jobSkills);

        // Candidate has Java verified, but Kubernetes and Python missing
        List<UserVerifiedSkillProjection> candidateSkills = List.of(
            new UserVerifiedSkillProjection(skillJava, "Java", true, "ADVANCED")
        );
        when(analyticsRepository.findCandidateSkills(eq(userId))).thenReturn(candidateSkills);

        List<SkillGapMetric> skillGaps = analyticsService.getSkillGaps(userId);

        assertThat(skillGaps).hasSize(3);

        // Unverified skills appear first: K8s (2/10 = 20.00%), then Python (1/10 = 10.00%), then Java (verified)
        assertThat(skillGaps.get(0).skillName()).isEqualTo("Kubernetes");
        assertThat(skillGaps.get(0).candidateVerified()).isFalse();
        assertThat(skillGaps.get(0).marketDemandPercent()).isEqualTo(new BigDecimal("20.00"));

        assertThat(skillGaps.get(1).skillName()).isEqualTo("Python");
        assertThat(skillGaps.get(1).candidateVerified()).isFalse();
        assertThat(skillGaps.get(1).marketDemandPercent()).isEqualTo(new BigDecimal("10.00"));

        assertThat(skillGaps.get(2).skillName()).isEqualTo("Java");
        assertThat(skillGaps.get(2).candidateVerified()).isTrue();
        assertThat(skillGaps.get(2).candidateProficiency()).isEqualTo("ADVANCED");
        assertThat(skillGaps.get(2).marketDemandPercent()).isEqualTo(new BigDecimal("30.00"));
    }
}
