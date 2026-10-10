package com.jobcommandcenter.analytics.domain;

import com.jobcommandcenter.application.domain.ApplicationSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Phase 11 Stage 1 - Analytics Domain Contracts & Calculation Rules Unit Tests")
class AnalyticsDomainContractsUnitTest {

    @Nested
    @DisplayName("StageConversionRate Tests")
    class StageConversionRateTests {

        @Test
        @DisplayName("Calculates accurate conversion percentage and scales to 2 decimal places")
        void shouldCalculatePercentageCorrectly() {
            StageConversionRate rate = StageConversionRate.of("APPLIED", "SCREENING", 80, 20, 3.5);

            assertThat(rate.fromStage()).isEqualTo("APPLIED");
            assertThat(rate.toStage()).isEqualTo("SCREENING");
            assertThat(rate.enteredCount()).isEqualTo(80);
            assertThat(rate.progressedCount()).isEqualTo(20);
            assertThat(rate.conversionRatePercent()).isEqualTo(new BigDecimal("25.00"));
            assertThat(rate.medianDaysInStage()).isEqualTo(3.5);
        }

        @Test
        @DisplayName("Returns 0.00% without division-by-zero when denominator is zero")
        void shouldHandleZeroDenominatorSafely() {
            StageConversionRate rate = StageConversionRate.of("APPLIED", "SCREENING", 0, 0, null);

            assertThat(rate.conversionRatePercent()).isEqualTo(new BigDecimal("0.00"));
            assertThat(rate.medianDaysInStage()).isNull();
        }

        @Test
        @DisplayName("Returns 0.00% when numerator is zero with non-zero denominator")
        void shouldHandleZeroNumeratorSafely() {
            StageConversionRate rate = StageConversionRate.of("SCREENING", "INTERVIEW", 50, 0, 2.0);

            assertThat(rate.conversionRatePercent()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Rounds fractional percentages correctly using HALF_UP")
        void shouldRoundPercentagesCorrectly() {
            // 1 out of 3 = 33.3333...% -> 33.33%
            BigDecimal result = StageConversionRate.calculatePercentage(1, 3);
            assertThat(result).isEqualTo(new BigDecimal("33.33"));

            // 2 out of 3 = 66.6666...% -> 66.67%
            BigDecimal result2 = StageConversionRate.calculatePercentage(2, 3);
            assertThat(result2).isEqualTo(new BigDecimal("66.67"));
        }

        @Test
        @DisplayName("Rejects negative counts or progressed count exceeding entered count")
        void shouldRejectInvalidCounts() {
            assertThatThrownBy(() -> StageConversionRate.of("APPLIED", "SCREENING", -1, 0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");

            assertThatThrownBy(() -> StageConversionRate.of("APPLIED", "SCREENING", 10, -5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");

            assertThatThrownBy(() -> StageConversionRate.of("APPLIED", "SCREENING", 10, 15, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed enteredCount");
        }

        @Test
        @DisplayName("Rejects negative median days in stage")
        void shouldRejectNegativeMedianDays() {
            assertThatThrownBy(() -> StageConversionRate.of("APPLIED", "SCREENING", 10, 5, -1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("medianDaysInStage cannot be negative");
        }

        @Test
        @DisplayName("Rejects null stages")
        void shouldRejectNullStages() {
            assertThatThrownBy(() -> new StageConversionRate(null, "SCREENING", 10, 5, null, null))
                .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> new StageConversionRate("APPLIED", null, 10, 5, null, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("SourceEffectiveness Tests")
    class SourceEffectivenessTests {

        @Test
        @DisplayName("Computes safe conversion percentages across stages for a source")
        void shouldCalculateSourceMetricsCorrectly() {
            SourceEffectiveness source = SourceEffectiveness.of(
                ApplicationSource.LINKEDIN,
                100,
                30,
                15,
                3
            );

            assertThat(source.source()).isEqualTo(ApplicationSource.LINKEDIN);
            assertThat(source.totalApplications()).isEqualTo(100);
            assertThat(source.screeningRatePercent()).isEqualTo(new BigDecimal("30.00"));
            assertThat(source.interviewRatePercent()).isEqualTo(new BigDecimal("15.00"));
            assertThat(source.offerRatePercent()).isEqualTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("Zero applications yields 0.00% without division-by-zero")
        void shouldHandleZeroApplicationsSafely() {
            SourceEffectiveness source = SourceEffectiveness.of(
                ApplicationSource.REFERRAL,
                0,
                0,
                0,
                0
            );

            assertThat(source.screeningRatePercent()).isEqualTo(new BigDecimal("0.00"));
            assertThat(source.interviewRatePercent()).isEqualTo(new BigDecimal("0.00"));
            assertThat(source.offerRatePercent()).isEqualTo(new BigDecimal("0.00"));
        }

        @Test
        @DisplayName("Rejects negative values or funnel anomalies")
        void shouldRejectInvalidValues() {
            assertThatThrownBy(() -> SourceEffectiveness.of(ApplicationSource.INDEED, -10, 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> SourceEffectiveness.of(ApplicationSource.INDEED, 10, 15, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed totalApplications");

            assertThatThrownBy(() -> SourceEffectiveness.of(null, 10, 5, 2, 1))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("SkillGapMetric Tests")
    class SkillGapMetricTests {

        @Test
        @DisplayName("Calculates market demand percentage correctly")
        void shouldComputeMarketDemandPercent() {
            UUID skillId = UUID.randomUUID();
            SkillGapMetric metric = SkillGapMetric.of(
                skillId,
                "Kubernetes",
                "DEVOPS",
                15,
                30,
                false,
                null
            );

            assertThat(metric.skillId()).isEqualTo(skillId);
            assertThat(metric.skillName()).isEqualTo("Kubernetes");
            assertThat(metric.category()).isEqualTo("DEVOPS");
            assertThat(metric.requiredJobCount()).isEqualTo(15);
            assertThat(metric.totalTargetJobs()).isEqualTo(30);
            assertThat(metric.marketDemandPercent()).isEqualTo(new BigDecimal("50.00"));
            assertThat(metric.candidateVerified()).isFalse();
            assertThat(metric.candidateProficiency()).isNull();
        }

        @Test
        @DisplayName("Handles zero target jobs without division-by-zero")
        void shouldHandleZeroTargetJobs() {
            UUID skillId = UUID.randomUUID();
            SkillGapMetric metric = SkillGapMetric.of(
                skillId,
                "Java",
                "BACKEND",
                0,
                0,
                true,
                "EXPERT"
            );

            assertThat(metric.marketDemandPercent()).isEqualTo(new BigDecimal("0.00"));
            assertThat(metric.candidateVerified()).isTrue();
            assertThat(metric.candidateProficiency()).isEqualTo("EXPERT");
        }

        @Test
        @DisplayName("Rejects invalid job counts or null identifiers")
        void shouldValidateSkillGapInputs() {
            UUID skillId = UUID.randomUUID();
            assertThatThrownBy(() -> SkillGapMetric.of(skillId, "Java", "BACKEND", 10, 5, true, "EXPERT"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed totalTargetJobs");

            assertThatThrownBy(() -> SkillGapMetric.of(null, "Java", "BACKEND", 5, 10, true, "EXPERT"))
                .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> SkillGapMetric.of(skillId, null, "BACKEND", 5, 10, true, "EXPERT"))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("FunnelMetrics Tests")
    class FunnelMetricsTests {

        @Test
        @DisplayName("Creates safe empty funnel metrics")
        void shouldCreateEmptyFunnelMetrics() {
            FunnelMetrics empty = FunnelMetrics.empty();

            assertThat(empty.totalApplications()).isEqualTo(0);
            assertThat(empty.activeApplications()).isEqualTo(0);
            assertThat(empty.screeningConversionRate()).isEqualTo(new BigDecimal("0.00"));
            assertThat(empty.assessmentConversionRate()).isEqualTo(new BigDecimal("0.00"));
            assertThat(empty.interviewConversionRate()).isEqualTo(new BigDecimal("0.00"));
            assertThat(empty.offerConversionRate()).isEqualTo(new BigDecimal("0.00"));
            assertThat(empty.overallAcceptanceRate()).isEqualTo(new BigDecimal("0.00"));
            assertThat(empty.stageConversions()).isEmpty();
        }

        @Test
        @DisplayName("Computes overall rates safely when populated")
        void shouldComputeOverallRatesCorrectly() {
            StageConversionRate stage1 = StageConversionRate.of("APPLIED", "SCREENING", 50, 25, 2.0);
            StageConversionRate stage2 = StageConversionRate.of("SCREENING", "INTERVIEW", 25, 10, 4.0);

            FunnelMetrics metrics = new FunnelMetrics(
                50, // total
                20, // active
                5,  // drafts
                50, // applied
                25, // screening
                15, // assessment
                10, // interview
                4,  // offer
                2,  // accepted
                15, // rejected
                3,  // withdrawn
                1,  // archived
                null, null, null, null, null, // Auto-computed rates
                List.of(stage1, stage2)
            );

            assertThat(metrics.screeningConversionRate()).isEqualTo(new BigDecimal("50.00")); // 25 / 50
            assertThat(metrics.assessmentConversionRate()).isEqualTo(new BigDecimal("30.00")); // 15 / 50
            assertThat(metrics.interviewConversionRate()).isEqualTo(new BigDecimal("20.00"));  // 10 / 50
            assertThat(metrics.offerConversionRate()).isEqualTo(new BigDecimal("8.00"));       // 4 / 50
            assertThat(metrics.overallAcceptanceRate()).isEqualTo(new BigDecimal("4.00"));      // 2 / 50
            assertThat(metrics.stageConversions()).hasSize(2);
        }

        @Test
        @DisplayName("Rejects negative metrics counts")
        void shouldRejectNegativeCounts() {
            assertThatThrownBy(() -> new FunnelMetrics(
                -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                null, null, null, null, null, Collections.emptyList()
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
