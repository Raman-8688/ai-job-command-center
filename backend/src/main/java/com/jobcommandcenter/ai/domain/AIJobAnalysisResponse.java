package com.jobcommandcenter.ai.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Structured analysis response returned by an AIProvider SPI implementation.
 */
public record AIJobAnalysisResponse(
    String normalizedTitle,
    String seniorityLevel,
    List<String> coreResponsibilities,
    List<String> inferredResponsibilities,
    List<AnalyzedTechnology> technologies,
    List<String> educationRequirements,
    List<String> certificationRequirements,
    String experienceExpectations,
    List<String> potentialRedFlags,
    BigDecimal confidence,
    String provider,
    String model,
    String promptVersion
) {
    public AIJobAnalysisResponse {
        coreResponsibilities = coreResponsibilities != null ? List.copyOf(coreResponsibilities) : List.of();
        inferredResponsibilities = inferredResponsibilities != null ? List.copyOf(inferredResponsibilities) : List.of();
        technologies = technologies != null ? List.copyOf(technologies) : List.of();
        educationRequirements = educationRequirements != null ? List.copyOf(educationRequirements) : List.of();
        certificationRequirements = certificationRequirements != null ? List.copyOf(certificationRequirements) : List.of();
        potentialRedFlags = potentialRedFlags != null ? List.copyOf(potentialRedFlags) : List.of();
        confidence = confidence != null ? confidence : new BigDecimal("0.85");
    }
}
