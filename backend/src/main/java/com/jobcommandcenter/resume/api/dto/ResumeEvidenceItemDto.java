package com.jobcommandcenter.resume.api.dto;

public record ResumeEvidenceItemDto(
    String skillOrRequirement,
    String section,
    String referenceTitle,
    String excerpt
) {}
