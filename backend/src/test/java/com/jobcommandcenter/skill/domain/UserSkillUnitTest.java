package com.jobcommandcenter.skill.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSkillUnitTest {

    @Test
    @DisplayName("AI_SUGGESTED skill claims must NEVER be automatically verified on creation (Anti-Hallucination)")
    void aiSuggestedSkillsMustNeverBeAutoVerified() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        // Even if request specifies verified = true with AI_SUGGESTED, domain entity strictly rejects auto-verification
        UserSkill userSkill = UserSkill.createNew(
            userId,
            skillId,
            SkillProficiency.INTERMEDIATE,
            new BigDecimal("2.5"),
            true, // attempt to auto-verify
            VerificationSource.AI_SUGGESTED,
            "Suggested by LLM parser"
        );

        assertThat(userSkill.isVerified()).isFalse();
        assertThat(userSkill.getLastVerifiedAt()).isNull();
        assertThat(userSkill.getVerificationSource()).isEqualTo(VerificationSource.AI_SUGGESTED);
    }

    @Test
    @DisplayName("USER_EXPLICIT skill claims can be created with verified status")
    void userExplicitSkillsCanBeVerified() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        UserSkill userSkill = UserSkill.createNew(
            userId,
            skillId,
            SkillProficiency.EXPERT,
            new BigDecimal("5.0"),
            true,
            VerificationSource.USER_EXPLICIT,
            "Self-certified"
        );

        assertThat(userSkill.isVerified()).isTrue();
        assertThat(userSkill.getLastVerifiedAt()).isNotNull();
        assertThat(userSkill.getVerificationSource()).isEqualTo(VerificationSource.USER_EXPLICIT);
    }

    @Test
    @DisplayName("Confirming verification cannot be done with AI_SUGGESTED source")
    void confirmVerificationRejectsAiSuggestedSource() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        UserSkill userSkill = UserSkill.createNew(
            userId,
            skillId,
            SkillProficiency.BEGINNER,
            BigDecimal.ONE,
            false,
            VerificationSource.AI_SUGGESTED,
            "Pending confirmation"
        );

        assertThatThrownBy(() -> userSkill.confirmVerification(VerificationSource.AI_SUGGESTED))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AI suggestions cannot verify skills");
    }

    @Test
    @DisplayName("Candidate confirming AI-suggested skill elevates source to USER_EXPLICIT")
    void confirmingAiSuggestedSkillElevatesToUserExplicit() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        UserSkill userSkill = UserSkill.createNew(
            userId,
            skillId,
            SkillProficiency.ADVANCED,
            new BigDecimal("3.0"),
            false,
            VerificationSource.AI_SUGGESTED,
            "AI recommended"
        );

        userSkill.updateDetails(null, null, true, null, "Approved by candidate");

        assertThat(userSkill.isVerified()).isTrue();
        assertThat(userSkill.getVerificationSource()).isEqualTo(VerificationSource.USER_EXPLICIT);
        assertThat(userSkill.getLastVerifiedAt()).isNotNull();
    }
}
