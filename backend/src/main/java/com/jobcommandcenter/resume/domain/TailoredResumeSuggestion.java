package com.jobcommandcenter.resume.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an individual section-level suggestion for tailoring.
 */
public class TailoredResumeSuggestion {

    private final UUID id;
    private final UUID tailoredResumeId;
    private final SectionType sectionType;
    private final String targetItemTitle;
    private final String originalContent;
    private final String suggestedContent;
    private final String rationale;
    private final String evidence;
    private final String verificationStatus;
    private boolean applied;
    private final int displayOrder;
    private final Instant createdAt;

    public TailoredResumeSuggestion(UUID id,
                                    UUID tailoredResumeId,
                                    SectionType sectionType,
                                    String targetItemTitle,
                                    String originalContent,
                                    String suggestedContent,
                                    String rationale,
                                    String evidence,
                                    String verificationStatus,
                                    boolean applied,
                                    int displayOrder,
                                    Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tailoredResumeId = Objects.requireNonNull(tailoredResumeId, "tailoredResumeId cannot be null");
        this.sectionType = Objects.requireNonNull(sectionType, "sectionType cannot be null");
        this.targetItemTitle = targetItemTitle;
        this.originalContent = originalContent;
        this.suggestedContent = Objects.requireNonNull(suggestedContent, "suggestedContent cannot be null");
        this.rationale = Objects.requireNonNull(rationale, "rationale cannot be null");
        this.evidence = evidence;
        this.verificationStatus = verificationStatus != null ? verificationStatus : "VERIFIED";
        this.applied = applied;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public void markApplied() {
        this.applied = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTailoredResumeId() {
        return tailoredResumeId;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public String getTargetItemTitle() {
        return targetItemTitle;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public String getSuggestedContent() {
        return suggestedContent;
    }

    public String getRationale() {
        return rationale;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public boolean isApplied() {
        return applied;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
