package com.jobcommandcenter.ai.api.dto;

import com.jobcommandcenter.ai.domain.JobAiFitEvaluation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record JobAiFitResponseDto(
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
) {
    public static JobAiFitResponseDto fromDomain(JobAiFitEvaluation evaluation) {
        return new JobAiFitResponseDto(
            evaluation.jobId(),
            evaluation.jobTitle(),
            evaluation.companyName(),
            evaluation.overallFitTier(),
            evaluation.deterministicScore(),
            evaluation.scoreBreakdown(),
            evaluation.matchedTechnologies(),
            evaluation.missingRequiredTechnologies(),
            evaluation.missingPreferredTechnologies(),
            evaluation.qualitativeSummary(),
            evaluation.interviewPreparationNotes(),
            evaluation.potentialRedFlags(),
            evaluation.confidence(),
            evaluation.analysisVersion()
        );
    }
}
