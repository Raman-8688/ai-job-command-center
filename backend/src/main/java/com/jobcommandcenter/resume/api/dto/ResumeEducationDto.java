package com.jobcommandcenter.resume.api.dto;

import java.util.UUID;

public record ResumeEducationDto(
    UUID id,
    String institution,
    String degree,
    String fieldOfStudy,
    Integer startYear,
    Integer endYear,
    int displayOrder
) {}
