package com.jobcommandcenter.resume;

import com.jobcommandcenter.resume.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TailoredResumeAggregateUnitTest {

    @Test
    @DisplayName("TailoredResume creates with initial DRAFT status and sets properties correctly")
    void tailoredResumeCreation() {
        UUID userId = UUID.randomUUID();
        UUID sourceResumeId = UUID.randomUUID();
        UUID targetJobId = UUID.randomUUID();

        TailoredResume draft = TailoredResume.createDraft(
            userId,
            sourceResumeId,
            targetJobId,
            1,
            "Senior Backend Engineer",
            "Tailored summary for Acme Corp.",
            new BigDecimal("85.00"),
            List.of("Java", "Spring Boot"),
            List.of("Kafka"),
            List.of()
        );

        assertThat(draft.getId()).isNotNull();
        assertThat(draft.getUserId()).isEqualTo(userId);
        assertThat(draft.getSourceResumeId()).isEqualTo(sourceResumeId);
        assertThat(draft.getTargetJobId()).isEqualTo(targetJobId);
        assertThat(draft.getVersion()).isEqualTo(1);
        assertThat(draft.getStatus()).isEqualTo(TailoredResumeStatus.DRAFT);
        assertThat(draft.getTailoredTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(draft.getKeywordCoverageScore()).isEqualByComparingTo("85.00");
        assertThat(draft.getMatchedKeywords()).containsExactly("Java", "Spring Boot");
        assertThat(draft.getMissingKeywords()).containsExactly("Kafka");
    }

    @Test
    @DisplayName("TailoredResume enforces human review status workflow")
    void workflowStatusTransitions() {
        TailoredResume draft = TailoredResume.createDraft(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            "Title",
            "Summary",
            BigDecimal.TEN,
            List.of(),
            List.of(),
            List.of()
        );

        assertThat(draft.getStatus()).isEqualTo(TailoredResumeStatus.DRAFT);

        draft.submitForReview();
        assertThat(draft.getStatus()).isEqualTo(TailoredResumeStatus.UNDER_REVIEW);

        draft.approve();
        assertThat(draft.getStatus()).isEqualTo(TailoredResumeStatus.APPROVED);

        assertThatThrownBy(draft::submitForReview)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Approved resume draft cannot be returned to review");
    }

    @Test
    @DisplayName("Applying suggestion updates section content and marks suggestion as applied")
    void applySuggestion() {
        UUID draftId = UUID.randomUUID();
        UUID sugId = UUID.randomUUID();

        TailoredResumeSuggestion suggestion = new TailoredResumeSuggestion(
            sugId,
            draftId,
            SectionType.SUMMARY,
            "Professional Summary",
            "Original summary",
            "Updated tailored executive summary",
            "Aligns with target role",
            "Verified Java skill",
            "VERIFIED",
            false,
            0,
            Instant.now()
        );

        TailoredResume draft = new TailoredResume(
            draftId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            TailoredResumeStatus.DRAFT,
            "Title",
            "Original summary",
            BigDecimal.TEN,
            List.of(),
            List.of(),
            List.of(suggestion),
            Instant.now(),
            Instant.now()
        );

        assertThat(draft.getTailoredSummary()).isEqualTo("Original summary");
        assertThat(draft.getSuggestions().get(0).isApplied()).isFalse();

        boolean result = draft.applySuggestion(sugId);

        assertThat(result).isTrue();
        assertThat(draft.getTailoredSummary()).isEqualTo("Updated tailored executive summary");
        assertThat(draft.getSuggestions().get(0).isApplied()).isTrue();
    }
}
