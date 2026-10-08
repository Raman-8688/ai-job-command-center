package com.jobcommandcenter.resume.api.dto;

import java.util.List;
import java.util.UUID;

public record ResumeProjectDto(
    UUID id,
    String projectName,
    String description,
    String role,
    List<String> technologies,
    List<String> responsibilities,
    List<String> achievements,
    String duration,
    String projectUrl,
    int displayOrder
) {}
