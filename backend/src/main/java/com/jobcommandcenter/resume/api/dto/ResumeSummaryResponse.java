package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.Resume;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ResumeSummaryResponse(
    UUID id,
    String name,
    String title,
    String status,
    BigDecimal yearsOfExperience,
    int skillsCount,
    int experienceCount,
    int projectsCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResumeSummaryResponse fromDomain(Resume domain) {
        return new ResumeSummaryResponse(
            domain.getId(),
            domain.getName(),
            domain.getTitle(),
            domain.getStatus().name(),
            domain.getYearsOfExperience(),
            domain.getSkills().size(),
            domain.getExperiences().size(),
            domain.getProjects().size(),
            domain.getCreatedAt(),
            domain.getUpdatedAt()
        );
    }
}
