package com.jobcommandcenter.email.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an ingested, synchronized email message.
 */
public class Email {

    private final UUID id;
    private final UUID userId;
    private UUID connectionId;
    private final String externalMessageId;
    private String externalThreadId;
    private String sender;
    private String recipient;
    private String subject;
    private String snippet;
    private String bodyPlain;
    private String bodyHtml;
    private Instant receivedAt;
    private EmailClassification classification;
    private BigDecimal classificationConfidence;
    private String classificationReason;
    private EmailProcessingStatus processingStatus;
    private String extractedCompanyName;
    private String extractedJobTitle;
    private String extractedExternalId;
    private String extractedNotes;
    private UUID associatedJobId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Email(
        UUID id,
        UUID userId,
        UUID connectionId,
        String externalMessageId,
        String externalThreadId,
        String sender,
        String recipient,
        String subject,
        String snippet,
        String bodyPlain,
        String bodyHtml,
        Instant receivedAt,
        EmailClassification classification,
        BigDecimal classificationConfidence,
        String classificationReason,
        EmailProcessingStatus processingStatus,
        String extractedCompanyName,
        String extractedJobTitle,
        String extractedExternalId,
        String extractedNotes,
        UUID associatedJobId,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "ID must not be null");
        this.userId = Objects.requireNonNull(userId, "User ID must not be null");
        this.connectionId = connectionId;
        this.externalMessageId = Objects.requireNonNull(externalMessageId, "External message ID must not be null");
        this.externalThreadId = externalThreadId;
        this.sender = Objects.requireNonNull(sender, "Sender must not be null");
        this.recipient = Objects.requireNonNull(recipient, "Recipient must not be null");
        this.subject = Objects.requireNonNull(subject, "Subject must not be null");
        this.snippet = snippet;
        this.bodyPlain = bodyPlain;
        this.bodyHtml = bodyHtml;
        this.receivedAt = Objects.requireNonNull(receivedAt, "Received at must not be null");
        this.classification = classification != null ? classification : EmailClassification.UNCLASSIFIED;
        this.classificationConfidence = classificationConfidence;
        this.classificationReason = classificationReason;
        this.processingStatus = processingStatus != null ? processingStatus : EmailProcessingStatus.UNPROCESSED;
        this.extractedCompanyName = extractedCompanyName;
        this.extractedJobTitle = extractedJobTitle;
        this.extractedExternalId = extractedExternalId;
        this.extractedNotes = extractedNotes;
        this.associatedJobId = associatedJobId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Email createNew(
        UUID userId,
        UUID connectionId,
        String externalMessageId,
        String externalThreadId,
        String sender,
        String recipient,
        String subject,
        String snippet,
        String bodyPlain,
        String bodyHtml,
        Instant receivedAt
    ) {
        Instant now = Instant.now();
        return new Email(
            UUID.randomUUID(),
            userId,
            connectionId,
            externalMessageId,
            externalThreadId,
            sender,
            recipient,
            subject,
            snippet,
            bodyPlain,
            bodyHtml,
            receivedAt,
            EmailClassification.UNCLASSIFIED,
            null,
            null,
            EmailProcessingStatus.UNPROCESSED,
            null,
            null,
            null,
            null,
            null,
            now,
            now
        );
    }

    public void updateClassification(EmailClassification classification, BigDecimal confidence, String reason) {
        this.classification = Objects.requireNonNull(classification, "Classification must not be null");
        this.classificationConfidence = confidence;
        this.classificationReason = reason;
        this.updatedAt = Instant.now();
    }

    public void updateProcessingStatus(EmailProcessingStatus status) {
        this.processingStatus = Objects.requireNonNull(status, "Processing status must not be null");
        this.updatedAt = Instant.now();
    }

    public void setExtractedDetails(String companyName, String jobTitle, String externalId, String notes) {
        if (companyName != null && !companyName.isBlank()) {
            this.extractedCompanyName = companyName.trim();
        }
        if (jobTitle != null && !jobTitle.isBlank()) {
            this.extractedJobTitle = jobTitle.trim();
        }
        if (externalId != null && !externalId.isBlank()) {
            this.extractedExternalId = externalId.trim();
        }
        if (notes != null && !notes.isBlank()) {
            this.extractedNotes = notes.trim();
        }
        this.updatedAt = Instant.now();
    }

    public void associateJob(UUID jobId) {
        this.associatedJobId = Objects.requireNonNull(jobId, "Job ID must not be null");
        this.processingStatus = EmailProcessingStatus.PROCESSED;
        this.updatedAt = Instant.now();
    }

    public void disassociateJob() {
        this.associatedJobId = null;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getConnectionId() { return connectionId; }
    public String getExternalMessageId() { return externalMessageId; }
    public String getExternalThreadId() { return externalThreadId; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getSnippet() { return snippet; }
    public String getBodyPlain() { return bodyPlain; }
    public String getBodyHtml() { return bodyHtml; }
    public Instant getReceivedAt() { return receivedAt; }
    public EmailClassification getClassification() { return classification; }
    public BigDecimal getClassificationConfidence() { return classificationConfidence; }
    public String getClassificationReason() { return classificationReason; }
    public EmailProcessingStatus getProcessingStatus() { return processingStatus; }
    public String getExtractedCompanyName() { return extractedCompanyName; }
    public String getExtractedJobTitle() { return extractedJobTitle; }
    public String getExtractedExternalId() { return extractedExternalId; }
    public String getExtractedNotes() { return extractedNotes; }
    public UUID getAssociatedJobId() { return associatedJobId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
