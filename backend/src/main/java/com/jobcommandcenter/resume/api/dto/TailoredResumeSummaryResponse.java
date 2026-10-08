package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.TailoredResume;
import com.jobcommandcenter.resume.domain.TailoredResumeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TailoredResumeSummaryResponse(
    UUID id,
    UUID sourceResumeId,
    UUID targetJobId,
    int version,
    TailoredResumeStatus status,
    String tailoredTitle,
    BigDecimal keywordCoverageScore,
    Instant createdAt,
    Instant updatedAt
) {
    public static TailoredResumeSummaryResponse fromDomain(TailoredResume domain) {
        return new TailoredResumeSummaryResponse(
            domain.getId(),
            domain.getSourceResumeId(),
            domain.getTargetJobId(),
            domain.getVersion(),
            domain.getStatus(),
            domain.getTailoredTitle(),
            domain.getKeywordCoverageScore(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
