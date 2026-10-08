package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.TailoredResume;
import com.jobcommandcenter.resume.domain.TailoredResumeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record TailoredResumeResponse(
    UUID id,
    UUID userId,
    UUID sourceResumeId,
    UUID targetJobId,
    int version,
    TailoredResumeStatus status,
    String tailoredTitle,
    String tailoredSummary,
    BigDecimal keywordCoverageScore,
    List<String> matchedKeywords,
    List<String> missingKeywords,
    List<TailoredResumeSuggestionDto> suggestions,
    Instant createdAt,
    Instant updatedAt
) {
    public static TailoredResumeResponse fromDomain(TailoredResume domain) {
        List<TailoredResumeSuggestionDto> suggestions = domain.getSuggestions().stream()
            .map(TailoredResumeSuggestionDto::fromDomain)
            .collect(Collectors.toList());

        return new TailoredResumeResponse(
            domain.getId(),
            domain.getUserId(),
            domain.getSourceResumeId(),
            domain.getTargetJobId(),
            domain.getVersion(),
            domain.getStatus(),
            domain.getTailoredTitle(),
            domain.getTailoredSummary(),
            domain.getKeywordCoverageScore(),
            domain.getMatchedKeywords(),
            domain.getMissingKeywords(),
            suggestions,
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
