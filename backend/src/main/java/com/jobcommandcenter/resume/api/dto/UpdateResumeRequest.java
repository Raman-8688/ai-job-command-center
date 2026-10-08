package com.jobcommandcenter.resume.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public record UpdateResumeRequest(
    @NotBlank(message = "Resume name cannot be blank")
    String name,
    String title,
    String summary,
    BigDecimal yearsOfExperience,
    String location,
    String contactEmail,
    String contactPhone,
    List<ResumeExperienceDto> experiences,
    List<ResumeProjectDto> projects,
    List<ResumeSkillDto> skills,
    List<ResumeEducationDto> education,
    List<ResumeCertificationDto> certifications
) {}
