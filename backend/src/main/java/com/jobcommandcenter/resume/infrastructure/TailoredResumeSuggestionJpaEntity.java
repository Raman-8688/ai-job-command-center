package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.resume.domain.SectionType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tailored_resume_suggestions")
public class TailoredResumeSuggestionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tailored_resume_id", insertable = false, updatable = false)
    private UUID tailoredResumeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 50)
    private SectionType sectionType;

    @Column(name = "target_item_title", length = 255)
    private String targetItemTitle;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "suggested_content", columnDefinition = "TEXT", nullable = false)
    private String suggestedContent;

    @Column(name = "rationale", columnDefinition = "TEXT", nullable = false)
    private String rationale;

    @Column(name = "evidence", columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "verification_status", length = 50, nullable = false)
    private String verificationStatus;

    @Column(name = "applied", nullable = false)
    private boolean applied;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TailoredResumeSuggestionJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTailoredResumeId() {
        return tailoredResumeId;
    }

    public void setTailoredResumeId(UUID tailoredResumeId) {
        this.tailoredResumeId = tailoredResumeId;
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public String getTargetItemTitle() {
        return targetItemTitle;
    }

    public void setTargetItemTitle(String targetItemTitle) {
        this.targetItemTitle = targetItemTitle;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public String getSuggestedContent() {
        return suggestedContent;
    }

    public void setSuggestedContent(String suggestedContent) {
        this.suggestedContent = suggestedContent;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
