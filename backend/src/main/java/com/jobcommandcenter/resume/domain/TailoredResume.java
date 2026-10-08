package com.jobcommandcenter.resume.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Domain Aggregate Root representing a versioned, job-specific tailored resume draft.
 * Maintains strict link to master resume, target job, and candidate ownership.
 */
public class TailoredResume {

    private final UUID id;
    private final UUID userId;
    private final UUID sourceResumeId;
    private final UUID targetJobId;
    private final int version;
    private TailoredResumeStatus status;
    private String tailoredTitle;
    private String tailoredSummary;
    private BigDecimal keywordCoverageScore;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<TailoredResumeSuggestion> suggestions;
    private final Instant createdAt;
    private Instant updatedAt;

    public TailoredResume(UUID id,
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
                          List<TailoredResumeSuggestion> suggestions,
                          Instant createdAt,
                          Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.sourceResumeId = Objects.requireNonNull(sourceResumeId, "sourceResumeId cannot be null");
        this.targetJobId = Objects.requireNonNull(targetJobId, "targetJobId cannot be null");
        this.version = version > 0 ? version : 1;
        this.status = status != null ? status : TailoredResumeStatus.DRAFT;
        this.tailoredTitle = tailoredTitle;
        this.tailoredSummary = tailoredSummary;
        this.keywordCoverageScore = keywordCoverageScore != null ? keywordCoverageScore : BigDecimal.ZERO;
        this.matchedKeywords = matchedKeywords != null ? new ArrayList<>(matchedKeywords) : new ArrayList<>();
        this.missingKeywords = missingKeywords != null ? new ArrayList<>(missingKeywords) : new ArrayList<>();
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static TailoredResume createDraft(UUID userId,
                                             UUID sourceResumeId,
                                             UUID targetJobId,
                                             int version,
                                             String tailoredTitle,
                                             String tailoredSummary,
                                             BigDecimal keywordCoverageScore,
                                             List<String> matchedKeywords,
                                             List<String> missingKeywords,
                                             List<TailoredResumeSuggestion> suggestions) {
        Instant now = Instant.now();
        return new TailoredResume(
            UUID.randomUUID(),
            userId,
            sourceResumeId,
            targetJobId,
            version,
            TailoredResumeStatus.DRAFT,
            tailoredTitle,
            tailoredSummary,
            keywordCoverageScore,
            matchedKeywords,
            missingKeywords,
            suggestions,
            now,
            now
        );
    }

    public void updateContent(String newTitle, String newSummary) {
        if (newTitle != null && !newTitle.isBlank()) {
            this.tailoredTitle = newTitle;
        }
        if (newSummary != null) {
            this.tailoredSummary = newSummary;
        }
        this.updatedAt = Instant.now();
    }

    public void submitForReview() {
        if (this.status == TailoredResumeStatus.APPROVED) {
            throw new IllegalStateException("Approved resume draft cannot be returned to review without new version");
        }
        this.status = TailoredResumeStatus.UNDER_REVIEW;
        this.updatedAt = Instant.now();
    }

    public void approve() {
        this.status = TailoredResumeStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void reject() {
        this.status = TailoredResumeStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void transitionStatus(TailoredResumeStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus cannot be null");
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    public boolean applySuggestion(UUID suggestionId) {
        for (TailoredResumeSuggestion suggestion : suggestions) {
            if (suggestion.getId().equals(suggestionId)) {
                suggestion.markApplied();
                if (suggestion.getSectionType() == SectionType.SUMMARY) {
                    this.tailoredSummary = suggestion.getSuggestedContent();
                }
                this.updatedAt = Instant.now();
                return true;
            }
        }
        return false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSourceResumeId() {
        return sourceResumeId;
    }

    public UUID getTargetJobId() {
        return targetJobId;
    }

    public int getVersion() {
        return version;
    }

    public TailoredResumeStatus getStatus() {
        return status;
    }

    public String getTailoredTitle() {
        return tailoredTitle;
    }

    public String getTailoredSummary() {
        return tailoredSummary;
    }

    public BigDecimal getKeywordCoverageScore() {
        return keywordCoverageScore;
    }

    public List<String> getMatchedKeywords() {
        return Collections.unmodifiableList(matchedKeywords);
    }

    public List<String> getMissingKeywords() {
        return Collections.unmodifiableList(missingKeywords);
    }

    public List<TailoredResumeSuggestion> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
