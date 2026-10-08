package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result of combining deterministic match score with AI qualitative analysis.
 * Deterministic data remains the foundation; AI provides semantic gap insights.
 */
public record JobAiFitEvaluation(
    UUID jobId,
    String jobTitle,
    String companyName,
    String overallFitTier,
    int deterministicScore,
    Map<String, Integer> scoreBreakdown,
    List<String> matchedTechnologies,
    List<String> missingRequiredTechnologies,
    List<String> missingPreferredTechnologies,
    String qualitativeSummary,
    List<String> interviewPreparationNotes,
    List<String> potentialRedFlags,
    BigDecimal confidence,
    int analysisVersion
) {}
