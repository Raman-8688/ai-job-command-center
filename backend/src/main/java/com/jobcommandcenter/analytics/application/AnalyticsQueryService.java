package com.jobcommandcenter.analytics.application;

import com.jobcommandcenter.analytics.domain.*;
import com.jobcommandcenter.analytics.infrastructure.persistence.*;
import com.jobcommandcenter.application.domain.ApplicationSource;
import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.assessment.domain.AssessmentResult;
import com.jobcommandcenter.assessment.domain.AssessmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core application service computing deterministic candidate pipeline analytics,
 * stage-by-stage funnel metrics, channel source effectiveness, and skill gap deficits.
 * Strictly read-only and tenant-isolated.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsQueryService {

    private final AnalyticsRepositoryPort analyticsRepository;

    public AnalyticsQueryService(AnalyticsRepositoryPort analyticsRepository) {
        this.analyticsRepository = Objects.requireNonNull(analyticsRepository, "analyticsRepository must not be null");
    }

    /**
     * Computes top-level career pipeline KPIs for the authenticated user.
     */
    public AnalyticsOverview getOverview(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        // 1. Applications counts by status
        List<Object[]> statusCounts = analyticsRepository.countApplicationsByStatus(userId);
        Map<ApplicationStatus, Long> appStatusMap = parseStatusMap(statusCounts);

        long totalApps = appStatusMap.values().stream().mapToLong(Long::longValue).sum();

        long activePipelines = appStatusMap.getOrDefault(ApplicationStatus.APPLIED, 0L)
            + appStatusMap.getOrDefault(ApplicationStatus.SCREENING, 0L)
            + appStatusMap.getOrDefault(ApplicationStatus.ASSESSMENT, 0L)
            + appStatusMap.getOrDefault(ApplicationStatus.INTERVIEW, 0L);

        long activeOffers = appStatusMap.getOrDefault(ApplicationStatus.OFFER, 0L);
        long rejections = appStatusMap.getOrDefault(ApplicationStatus.REJECTED, 0L);

        // 2. Interviews count
        List<Object[]> interviewCounts = analyticsRepository.countInterviewsByRoundAndOutcome(userId);
        long totalInterviews = interviewCounts.stream()
            .mapToLong(row -> ((Number) row[2]).longValue())
            .sum();

        // 3. Assessments count & pass rate
        List<Object[]> assessmentCounts = analyticsRepository.countAssessmentsByStatusAndResult(userId);
        long totalAssessments = 0L;
        long passedAssessments = 0L;
        long evaluatedAssessments = 0L;

        for (Object[] row : assessmentCounts) {
            AssessmentStatus status = (AssessmentStatus) row[0];
            AssessmentResult result = (AssessmentResult) row[1];
            long count = ((Number) row[2]).longValue();
            totalAssessments += count;

            // Evaluated assessments = completed/submitted with a decisive result (PASSED or FAILED)
            if (result == AssessmentResult.PASSED) {
                passedAssessments += count;
                evaluatedAssessments += count;
            } else if (result == AssessmentResult.FAILED) {
                evaluatedAssessments += count;
            }
        }

        BigDecimal interviewConversionRate = StageConversionRate.calculatePercentage(
            appStatusMap.getOrDefault(ApplicationStatus.INTERVIEW, 0L)
                + appStatusMap.getOrDefault(ApplicationStatus.OFFER, 0L)
                + appStatusMap.getOrDefault(ApplicationStatus.ACCEPTED, 0L),
            totalApps
        );

        BigDecimal assessmentPassRate = StageConversionRate.calculatePercentage(passedAssessments, evaluatedAssessments);
        BigDecimal offerRate = StageConversionRate.calculatePercentage(activeOffers + appStatusMap.getOrDefault(ApplicationStatus.ACCEPTED, 0L), totalApps);

        return new AnalyticsOverview(
            totalApps,
            activePipelines,
            totalInterviews,
            totalAssessments,
            activeOffers,
            rejections,
            interviewConversionRate,
            assessmentPassRate,
            offerRate
        );
    }

    /**
     * Computes step-by-step funnel metrics, stage entry counts, drop-offs, and median duration.
     */
    public FunnelMetrics getFunnelMetrics(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        List<Object[]> statusCounts = analyticsRepository.countApplicationsByStatus(userId);
        Map<ApplicationStatus, Long> appStatusMap = parseStatusMap(statusCounts);

        long totalApps = appStatusMap.values().stream().mapToLong(Long::longValue).sum();
        if (totalApps == 0) {
            return FunnelMetrics.empty();
        }

        long drafts = appStatusMap.getOrDefault(ApplicationStatus.DRAFT, 0L);
        long applied = appStatusMap.getOrDefault(ApplicationStatus.APPLIED, 0L);
        long screening = appStatusMap.getOrDefault(ApplicationStatus.SCREENING, 0L);
        long assessment = appStatusMap.getOrDefault(ApplicationStatus.ASSESSMENT, 0L);
        long interview = appStatusMap.getOrDefault(ApplicationStatus.INTERVIEW, 0L);
        long offer = appStatusMap.getOrDefault(ApplicationStatus.OFFER, 0L);
        long accepted = appStatusMap.getOrDefault(ApplicationStatus.ACCEPTED, 0L);
        long rejected = appStatusMap.getOrDefault(ApplicationStatus.REJECTED, 0L);
        long withdrawn = appStatusMap.getOrDefault(ApplicationStatus.WITHDRAWN, 0L);
        long archived = appStatusMap.getOrDefault(ApplicationStatus.ARCHIVED, 0L);

        long activeApps = applied + screening + assessment + interview;

        // Stage history analysis via event transitions
        List<StatusTransitionProjection> transitions = analyticsRepository.findApplicationTransitions(userId);

        // Track distinct applications that reached each lifecycle milestone
        Set<UUID> reachedApplied = new HashSet<>();
        Set<UUID> reachedScreening = new HashSet<>();
        Set<UUID> reachedAssessment = new HashSet<>();
        Set<UUID> reachedInterview = new HashSet<>();
        Set<UUID> reachedOffer = new HashSet<>();
        Set<UUID> reachedAccepted = new HashSet<>();

        // Map for recording stage durations: stage -> list of durations in days
        Map<String, List<Double>> stageDurations = new HashMap<>();

        // Group transitions by applicationId
        Map<UUID, List<StatusTransitionProjection>> transitionsByApp = transitions.stream()
            .collect(Collectors.groupingBy(StatusTransitionProjection::applicationId));

        for (Map.Entry<UUID, List<StatusTransitionProjection>> entry : transitionsByApp.entrySet()) {
            UUID appId = entry.getKey();
            List<StatusTransitionProjection> appEvents = entry.getValue();

            for (int i = 0; i < appEvents.size(); i++) {
                StatusTransitionProjection ev = appEvents.get(i);
                String newSt = ev.newStatus();

                if ("APPLIED".equals(newSt)) reachedApplied.add(appId);
                else if ("SCREENING".equals(newSt)) reachedScreening.add(appId);
                else if ("ASSESSMENT".equals(newSt)) reachedAssessment.add(appId);
                else if ("INTERVIEW".equals(newSt)) reachedInterview.add(appId);
                else if ("OFFER".equals(newSt)) reachedOffer.add(appId);
                else if ("ACCEPTED".equals(newSt)) reachedAccepted.add(appId);

                // Compute duration spent in previous stage if next transition exists
                if (i > 0) {
                    StatusTransitionProjection prevEv = appEvents.get(i - 1);
                    if (prevEv.occurredAt() != null && ev.occurredAt() != null) {
                        double days = Duration.between(prevEv.occurredAt(), ev.occurredAt()).toMinutes() / (24.0 * 60.0);
                        if (days >= 0.0) {
                            String stageName = prevEv.newStatus();
                            stageDurations.computeIfAbsent(stageName, k -> new ArrayList<>()).add(days);
                        }
                    }
                }
            }
        }

        // Base denominator for funnel transitions:
        // Candidates who submitted applications
        long countApplied = Math.max(reachedApplied.size(), applied + screening + assessment + interview + offer + accepted + rejected);
        long countScreening = Math.max(reachedScreening.size(), screening + assessment + interview + offer + accepted);
        long countInterview = Math.max(reachedInterview.size(), interview + offer + accepted);
        long countOffer = Math.max(reachedOffer.size(), offer + accepted);
        long countAccepted = Math.max(reachedAccepted.size(), accepted);

        // Ensure hierarchical invariant: child stage cannot exceed parent stage
        countScreening = Math.min(countScreening, countApplied);
        countInterview = Math.min(countInterview, countScreening);
        countOffer = Math.min(countOffer, countInterview);
        countAccepted = Math.min(countAccepted, countOffer);

        List<StageConversionRate> conversions = new ArrayList<>();
        conversions.add(StageConversionRate.of(
            "APPLIED", "SCREENING", countApplied, countScreening, calculateMedian(stageDurations.get("APPLIED"))
        ));
        conversions.add(StageConversionRate.of(
            "SCREENING", "INTERVIEW", countScreening, countInterview, calculateMedian(stageDurations.get("SCREENING"))
        ));
        conversions.add(StageConversionRate.of(
            "INTERVIEW", "OFFER", countInterview, countOffer, calculateMedian(stageDurations.get("INTERVIEW"))
        ));
        conversions.add(StageConversionRate.of(
            "OFFER", "ACCEPTED", countOffer, countAccepted, calculateMedian(stageDurations.get("OFFER"))
        ));

        return new FunnelMetrics(
            totalApps,
            activeApps,
            drafts,
            applied,
            screening,
            assessment,
            interview,
            offer,
            accepted,
            rejected,
            withdrawn,
            archived,
            StageConversionRate.calculatePercentage(countScreening, countApplied),
            StageConversionRate.calculatePercentage(assessment, countApplied),
            StageConversionRate.calculatePercentage(countInterview, countApplied),
            StageConversionRate.calculatePercentage(countOffer, countApplied),
            StageConversionRate.calculatePercentage(countAccepted, countApplied),
            conversions
        );
    }

    /**
     * Evaluates candidate application performance and conversion broken down by submission source/channel.
     */
    public List<SourceEffectiveness> getSourceEffectiveness(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        List<Object[]> sourceStatusRows = analyticsRepository.countApplicationsBySourceAndStatus(userId);

        Map<ApplicationSource, Map<ApplicationStatus, Long>> sourceMap = new EnumMap<>(ApplicationSource.class);

        for (Object[] row : sourceStatusRows) {
            ApplicationSource source = row[0] != null ? (ApplicationSource) row[0] : ApplicationSource.MANUAL;
            ApplicationStatus status = (ApplicationStatus) row[1];
            long count = ((Number) row[2]).longValue();

            sourceMap.computeIfAbsent(source, s -> new EnumMap<>(ApplicationStatus.class))
                .merge(status, count, Long::sum);
        }

        List<SourceEffectiveness> results = new ArrayList<>();

        for (Map.Entry<ApplicationSource, Map<ApplicationStatus, Long>> entry : sourceMap.entrySet()) {
            ApplicationSource source = entry.getKey();
            Map<ApplicationStatus, Long> counts = entry.getValue();

            long total = counts.values().stream().mapToLong(Long::longValue).sum();

            // Progressions:
            // Screenings reached: status in SCREENING, ASSESSMENT, INTERVIEW, OFFER, ACCEPTED
            long screenings = counts.getOrDefault(ApplicationStatus.SCREENING, 0L)
                + counts.getOrDefault(ApplicationStatus.ASSESSMENT, 0L)
                + counts.getOrDefault(ApplicationStatus.INTERVIEW, 0L)
                + counts.getOrDefault(ApplicationStatus.OFFER, 0L)
                + counts.getOrDefault(ApplicationStatus.ACCEPTED, 0L);

            // Interviews reached: status in INTERVIEW, OFFER, ACCEPTED
            long interviews = counts.getOrDefault(ApplicationStatus.INTERVIEW, 0L)
                + counts.getOrDefault(ApplicationStatus.OFFER, 0L)
                + counts.getOrDefault(ApplicationStatus.ACCEPTED, 0L);

            // Offers received: status in OFFER, ACCEPTED
            long offers = counts.getOrDefault(ApplicationStatus.OFFER, 0L)
                + counts.getOrDefault(ApplicationStatus.ACCEPTED, 0L);

            results.add(SourceEffectiveness.of(source, total, screenings, interviews, offers));
        }

        // Sort descending by totalApplications, then by offerRatePercent
        results.sort(Comparator.comparing(SourceEffectiveness::totalApplications)
            .thenComparing(SourceEffectiveness::offerRatePercent).reversed());

        return results;
    }

    /**
     * Evaluates missing/unverified skill gaps between target job demands and candidate verified skills.
     */
    public List<SkillGapMetric> getSkillGaps(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        long targetJobsCount = analyticsRepository.countDistinctTargetJobs(userId);
        if (targetJobsCount == 0) {
            return Collections.emptyList();
        }

        List<JobSkillRequirementProjection> jobSkills = analyticsRepository.findTargetJobSkillRequirements(userId);
        List<UserVerifiedSkillProjection> candidateSkills = analyticsRepository.findCandidateSkills(userId);

        // Map candidate verified skills: skillId -> UserVerifiedSkillProjection
        Map<UUID, UserVerifiedSkillProjection> verifiedMap = candidateSkills.stream()
            .collect(Collectors.toMap(UserVerifiedSkillProjection::skillId, p -> p, (a, b) -> a));

        // Group job requirements by skillId
        Map<UUID, List<JobSkillRequirementProjection>> groupedBySkill = jobSkills.stream()
            .collect(Collectors.groupingBy(JobSkillRequirementProjection::skillId));

        List<SkillGapMetric> metrics = new ArrayList<>();

        for (Map.Entry<UUID, List<JobSkillRequirementProjection>> entry : groupedBySkill.entrySet()) {
            UUID skillId = entry.getKey();
            List<JobSkillRequirementProjection> reqs = entry.getValue();
            JobSkillRequirementProjection first = reqs.get(0);

            // Count distinct jobs requiring this skill
            long distinctJobsRequiring = reqs.stream().map(JobSkillRequirementProjection::jobId).distinct().count();

            UserVerifiedSkillProjection userSkill = verifiedMap.get(skillId);
            boolean isVerified = userSkill != null && userSkill.verified();
            String proficiency = userSkill != null ? userSkill.proficiency() : null;

            metrics.add(SkillGapMetric.of(
                skillId,
                first.skillName(),
                first.category(),
                distinctJobsRequiring,
                targetJobsCount,
                isVerified,
                proficiency
            ));
        }

        // Sort by candidateVerified ASC (unverified first), then marketDemandPercent DESC
        metrics.sort(Comparator.comparing(SkillGapMetric::candidateVerified)
            .thenComparing(Comparator.comparing(SkillGapMetric::marketDemandPercent).reversed()));

        return metrics;
    }

    // Helper methods
    private Map<ApplicationStatus, Long> parseStatusMap(List<Object[]> rows) {
        Map<ApplicationStatus, Long> map = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : rows) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            map.put(status, count);
        }
        return map;
    }

    private Double calculateMedian(List<Double> durations) {
        if (durations == null || durations.isEmpty()) {
            return null;
        }
        List<Double> sorted = new ArrayList<>(durations);
        Collections.sort(sorted);
        int size = sorted.size();
        double median;
        if (size % 2 == 1) {
            median = sorted.get(size / 2);
        } else {
            median = (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }
        return BigDecimal.valueOf(median).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
