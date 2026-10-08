package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.resume.domain.TailoredResumeStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tailored_resumes", uniqueConstraints = {
    @UniqueConstraint(name = "uq_tailored_resume_version", columnNames = {"source_resume_id", "target_job_id", "version"})
})
public class TailoredResumeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "source_resume_id", nullable = false)
    private UUID sourceResumeId;

    @Column(name = "target_job_id", nullable = false)
    private UUID targetJobId;

    @Column(name = "version", nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TailoredResumeStatus status;

    @Column(name = "tailored_title", length = 255)
    private String tailoredTitle;

    @Column(name = "tailored_summary", columnDefinition = "TEXT")
    private String tailoredSummary;

    @Column(name = "keyword_coverage_score", precision = 5, scale = 2)
    private BigDecimal keywordCoverageScore;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "tailored_resume_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<TailoredResumeSuggestionJpaEntity> suggestions = new ArrayList<>();

    public TailoredResumeJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSourceResumeId() {
        return sourceResumeId;
    }

    public void setSourceResumeId(UUID sourceResumeId) {
        this.sourceResumeId = sourceResumeId;
    }

    public UUID getTargetJobId() {
        return targetJobId;
    }

    public void setTargetJobId(UUID targetJobId) {
        this.targetJobId = targetJobId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public TailoredResumeStatus getStatus() {
        return status;
    }

    public void setStatus(TailoredResumeStatus status) {
        this.status = status;
    }

    public String getTailoredTitle() {
        return tailoredTitle;
    }

    public void setTailoredTitle(String tailoredTitle) {
        this.tailoredTitle = tailoredTitle;
    }

    public String getTailoredSummary() {
        return tailoredSummary;
    }

    public void setTailoredSummary(String tailoredSummary) {
        this.tailoredSummary = tailoredSummary;
    }

    public BigDecimal getKeywordCoverageScore() {
        return keywordCoverageScore;
    }

    public void setKeywordCoverageScore(BigDecimal keywordCoverageScore) {
        this.keywordCoverageScore = keywordCoverageScore;
    }

    public String getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(String matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public String getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(String missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TailoredResumeSuggestionJpaEntity> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<TailoredResumeSuggestionJpaEntity> suggestions) {
        this.suggestions = suggestions;
    }
}
