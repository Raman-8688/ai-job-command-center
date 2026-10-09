package com.jobcommandcenter.email.infrastructure.persistence;

import com.jobcommandcenter.email.domain.Email;
import com.jobcommandcenter.email.domain.EmailClassification;
import com.jobcommandcenter.email.domain.EmailProcessingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for emails table.
 */
@Entity
@Table(name = "emails")
public class EmailJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "connection_id")
    private UUID connectionId;

    @Column(name = "external_message_id", nullable = false)
    private String externalMessageId;

    @Column(name = "external_thread_id")
    private String externalThreadId;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "snippet", columnDefinition = "TEXT")
    private String snippet;

    @Column(name = "body_plain", columnDefinition = "TEXT")
    private String bodyPlain;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 50)
    private EmailClassification classification;

    @Column(name = "classification_confidence", precision = 4, scale = 3)
    private BigDecimal classificationConfidence;

    @Column(name = "classification_reason", columnDefinition = "TEXT")
    private String classificationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 50)
    private EmailProcessingStatus processingStatus;

    @Column(name = "extracted_company_name")
    private String extractedCompanyName;

    @Column(name = "extracted_job_title")
    private String extractedJobTitle;

    @Column(name = "extracted_external_id", length = 150)
    private String extractedExternalId;

    @Column(name = "extracted_notes", columnDefinition = "TEXT")
    private String extractedNotes;

    @Column(name = "associated_job_id")
    private UUID associatedJobId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EmailJpaEntity() {}

    public static EmailJpaEntity fromDomain(Email domain) {
        EmailJpaEntity entity = new EmailJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.connectionId = domain.getConnectionId();
        entity.externalMessageId = domain.getExternalMessageId();
        entity.externalThreadId = domain.getExternalThreadId();
        entity.sender = domain.getSender();
        entity.recipient = domain.getRecipient();
        entity.subject = domain.getSubject();
        entity.snippet = domain.getSnippet();
        entity.bodyPlain = domain.getBodyPlain();
        entity.bodyHtml = domain.getBodyHtml();
        entity.receivedAt = domain.getReceivedAt();
        entity.classification = domain.getClassification();
        entity.classificationConfidence = domain.getClassificationConfidence();
        entity.classificationReason = domain.getClassificationReason();
        entity.processingStatus = domain.getProcessingStatus();
        entity.extractedCompanyName = domain.getExtractedCompanyName();
        entity.extractedJobTitle = domain.getExtractedJobTitle();
        entity.extractedExternalId = domain.getExtractedExternalId();
        entity.extractedNotes = domain.getExtractedNotes();
        entity.associatedJobId = domain.getAssociatedJobId();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    public Email toDomain() {
        return new Email(
            this.id,
            this.userId,
            this.connectionId,
            this.externalMessageId,
            this.externalThreadId,
            this.sender,
            this.recipient,
            this.subject,
            this.snippet,
            this.bodyPlain,
            this.bodyHtml,
            this.receivedAt,
            this.classification,
            this.classificationConfidence,
            this.classificationReason,
            this.processingStatus,
            this.extractedCompanyName,
            this.extractedJobTitle,
            this.extractedExternalId,
            this.extractedNotes,
            this.associatedJobId,
            this.createdAt,
            this.updatedAt
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getConnectionId() { return connectionId; }
    public void setConnectionId(UUID connectionId) { this.connectionId = connectionId; }
    public String getExternalMessageId() { return externalMessageId; }
    public void setExternalMessageId(String externalMessageId) { this.externalMessageId = externalMessageId; }
    public String getExternalThreadId() { return externalThreadId; }
    public void setExternalThreadId(String externalThreadId) { this.externalThreadId = externalThreadId; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
    public String getBodyPlain() { return bodyPlain; }
    public void setBodyPlain(String bodyPlain) { this.bodyPlain = bodyPlain; }
    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public EmailClassification getClassification() { return classification; }
    public void setClassification(EmailClassification classification) { this.classification = classification; }
    public BigDecimal getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(BigDecimal classificationConfidence) { this.classificationConfidence = classificationConfidence; }
    public String getClassificationReason() { return classificationReason; }
    public void setClassificationReason(String classificationReason) { this.classificationReason = classificationReason; }
    public EmailProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(EmailProcessingStatus processingStatus) { this.processingStatus = processingStatus; }
    public String getExtractedCompanyName() { return extractedCompanyName; }
    public void setExtractedCompanyName(String extractedCompanyName) { this.extractedCompanyName = extractedCompanyName; }
    public String getExtractedJobTitle() { return extractedJobTitle; }
    public void setExtractedJobTitle(String extractedJobTitle) { this.extractedJobTitle = extractedJobTitle; }
    public String getExtractedExternalId() { return extractedExternalId; }
    public void setExtractedExternalId(String extractedExternalId) { this.extractedExternalId = extractedExternalId; }
    public String getExtractedNotes() { return extractedNotes; }
    public void setExtractedNotes(String extractedNotes) { this.extractedNotes = extractedNotes; }
    public UUID getAssociatedJobId() { return associatedJobId; }
    public void setAssociatedJobId(UUID associatedJobId) { this.associatedJobId = associatedJobId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
