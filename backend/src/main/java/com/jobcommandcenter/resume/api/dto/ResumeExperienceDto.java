package com.jobcommandcenter.resume.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ResumeExperienceDto(
    UUID id,
    String company,
    String jobTitle,
    LocalDate startDate,
    LocalDate endDate,
    boolean currentlyWorking,
    String location,
    String description,
    List<String> achievements,
    List<String> technologies,
    int displayOrder
) {}
