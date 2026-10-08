package com.jobcommandcenter.ai.domain;

/**
 * Section-level tailoring recommendation produced by AI layer.
 * Strictly distinguishes grounded, verified items from missing evidence.
 */
public record AISuggestionItem(
    String sectionType,
    String targetItemTitle,
    String originalContent,
    String suggestedContent,
    String rationale,
    String evidence,
    String verificationStatus
) {
}
