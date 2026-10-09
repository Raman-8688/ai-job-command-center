package com.jobcommandcenter.assessment.api.dto;

import com.jobcommandcenter.assessment.domain.CompanyDossier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CompanyDossierResponse(
    UUID id,
    UUID userId,
    UUID jobId,
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
    BigDecimal confidenceScore,
    Instant createdAt,
    Instant updatedAt
) {
    public static CompanyDossierResponse fromDomain(CompanyDossier domain, String provenanceSummary, BigDecimal confidenceScore) {
        return new CompanyDossierResponse(
            domain.getId(),
            domain.getUserId(),
            domain.getJobId(),
            domain.getCompanyName(),
            domain.getCompanyTier(),
            domain.getOverview(),
            domain.getEngineeringScale(),
            domain.getCoreTechStack(),
            domain.getEngineeringCulture(),
            domain.getArchitectureFocus(),
            domain.getTailoredTalkingPoints(),
            domain.getInterviewerQuestions(),
            provenanceSummary,
            confidenceScore,
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
