package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;

/**
 * Structured AI response containing executive company technical intelligence,
 * architecture notes, tailored talking points, and interviewer questions.
 */
public record AICompanyDossierResponse(
    String companyName,
    String companyTier,
    String overview,
    String engineeringScale,
    String coreTechStack,
    String engineeringCulture,
    String architectureFocus,
    String tailoredTalkingPoints,
    String interviewerQuestions,
    String provenanceSummary,
    BigDecimal confidenceScore
) {
    public AICompanyDossierResponse {
        if (companyTier == null || companyTier.isBlank()) {
            companyTier = "ENTERPRISE";
        }
        if (confidenceScore == null) {
            confidenceScore = new BigDecimal("0.85");
        }
    }
}
