package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.Resume;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ResumeResponse(
    UUID id,
    UUID userId,
    String name,
    String title,
    String summary,
    BigDecimal yearsOfExperience,
    String location,
    String contactEmail,
    String contactPhone,
    String status,
    Instant createdAt,
    Instant updatedAt,
    List<ResumeExperienceDto> experiences,
    List<ResumeProjectDto> projects,
    List<ResumeSkillDto> skills,
    List<ResumeEducationDto> education,
    List<ResumeCertificationDto> certifications
) {
    public static ResumeResponse fromDomain(Resume domain) {
        List<ResumeExperienceDto> expDtos = domain.getExperiences().stream()
            .map(e -> new ResumeExperienceDto(
                e.getId(),
                e.getCompany(),
                e.getJobTitle(),
                e.getStartDate(),
                e.getEndDate(),
                e.isCurrentlyWorking(),
                e.getLocation(),
                e.getDescription(),
                e.getAchievements(),
                e.getTechnologies(),
                e.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeProjectDto> projDtos = domain.getProjects().stream()
            .map(p -> new ResumeProjectDto(
                p.getId(),
                p.getProjectName(),
                p.getDescription(),
                p.getRole(),
                p.getTechnologies(),
                p.getResponsibilities(),
                p.getAchievements(),
                p.getDuration(),
                p.getProjectUrl(),
                p.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeSkillDto> skillDtos = domain.getSkills().stream()
            .map(s -> new ResumeSkillDto(
                s.getId(),
                s.getSkillId(),
                s.getSkillName(),
                s.getProficiency(),
                s.getYearsExperience()
            ))
            .collect(Collectors.toList());

        List<ResumeEducationDto> eduDtos = domain.getEducation().stream()
            .map(ed -> new ResumeEducationDto(
                ed.getId(),
                ed.getInstitution(),
                ed.getDegree(),
                ed.getFieldOfStudy(),
                ed.getStartYear(),
                ed.getEndYear(),
                ed.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        List<ResumeCertificationDto> certDtos = domain.getCertifications().stream()
            .map(c -> new ResumeCertificationDto(
                c.getId(),
                c.getName(),
                c.getIssuingOrganization(),
                c.getIssueDate(),
                c.getExpiryDate(),
                c.getCredentialId(),
                c.getCredentialUrl(),
                c.getDisplayOrder()
            ))
            .collect(Collectors.toList());

        return new ResumeResponse(
            domain.getId(),
            domain.getUserId(),
            domain.getName(),
            domain.getTitle(),
            domain.getSummary(),
            domain.getYearsOfExperience(),
            domain.getLocation(),
            domain.getContactEmail(),
            domain.getContactPhone(),
            domain.getStatus().name(),
            domain.getCreatedAt(),
            domain.getUpdatedAt(),
            expDtos,
            projDtos,
            skillDtos,
            eduDtos,
            certDtos
        );
    }
}
