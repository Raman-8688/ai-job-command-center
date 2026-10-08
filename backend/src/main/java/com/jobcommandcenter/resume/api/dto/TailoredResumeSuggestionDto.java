package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.SectionType;
import com.jobcommandcenter.resume.domain.TailoredResumeSuggestion;

import java.time.Instant;
import java.util.UUID;

public record TailoredResumeSuggestionDto(
    UUID id,
    SectionType sectionType,
    String targetItemTitle,
    String originalContent,
    String suggestedContent,
    String rationale,
    String evidence,
    String verificationStatus,
    boolean applied,
    int displayOrder,
    Instant createdAt
) {
    public static TailoredResumeSuggestionDto fromDomain(TailoredResumeSuggestion domain) {
        return new TailoredResumeSuggestionDto(
            domain.getId(),
            domain.getSectionType(),
            domain.getTargetItemTitle(),
            domain.getOriginalContent(),
            domain.getSuggestedContent(),
            domain.getRationale(),
            domain.getEvidence(),
            domain.getVerificationStatus(),
            domain.isApplied(),
            domain.getDisplayOrder(),
            domain.getCreatedAt()
        );
    }
}
