package com.jobcommandcenter.resume.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a professional certification on a resume.
 */
public class ResumeCertification {

    private final UUID id;
    private String name;
    private String issuingOrganization;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialId;
    private String credentialUrl;
    private int displayOrder;

    public ResumeCertification(UUID id,
                               String name,
                               String issuingOrganization,
                               LocalDate issueDate,
                               LocalDate expiryDate,
                               String credentialId,
                               String credentialUrl,
                               int displayOrder) {
        this.id = Objects.requireNonNull(id, "Certification ID cannot be null");
        this.name = Objects.requireNonNull(name, "Certification name cannot be null").trim();
        this.issuingOrganization = Objects.requireNonNull(issuingOrganization, "Issuing organization cannot be null").trim();
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.credentialId = credentialId;
        this.credentialUrl = credentialUrl;
        this.displayOrder = displayOrder;
    }

    public static ResumeCertification create(String name,
                                             String issuingOrganization,
                                             LocalDate issueDate,
                                             LocalDate expiryDate,
                                             String credentialId,
                                             String credentialUrl,
                                             int displayOrder) {
        return new ResumeCertification(
            UUID.randomUUID(),
            name,
            issuingOrganization,
            issueDate,
            expiryDate,
            credentialId,
            credentialUrl,
            displayOrder
        );
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getIssuingOrganization() { return issuingOrganization; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCredentialId() { return credentialId; }
    public String getCredentialUrl() { return credentialUrl; }
    public int getDisplayOrder() { return displayOrder; }

    public void update(String name,
                       String issuingOrganization,
                       LocalDate issueDate,
                       LocalDate expiryDate,
                       String credentialId,
                       String credentialUrl,
                       int displayOrder) {
        this.name = Objects.requireNonNull(name, "Certification name cannot be null").trim();
        this.issuingOrganization = Objects.requireNonNull(issuingOrganization, "Issuing organization cannot be null").trim();
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.credentialId = credentialId;
        this.credentialUrl = credentialUrl;
        this.displayOrder = displayOrder;
    }
}
