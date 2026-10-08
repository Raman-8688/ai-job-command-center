package com.jobcommandcenter.resume.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ResumeCertificationDto(
    UUID id,
    String name,
    String issuingOrganization,
    LocalDate issueDate,
    LocalDate expiryDate,
    String credentialId,
    String credentialUrl,
    int displayOrder
) {}
