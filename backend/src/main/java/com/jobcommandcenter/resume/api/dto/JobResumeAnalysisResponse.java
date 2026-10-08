package com.jobcommandcenter.resume.api.dto;

import com.jobcommandcenter.resume.domain.JobResumeAnalysisResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record JobResumeAnalysisResponse(
    UUID resumeId,
    UUID jobId,
    String resumeName,
    String jobTitle,
    String companyName,
    List<String> strongMatches,
    List<String> missingRequiredSkills,
    List<String> missingPreferredSkills,
    List<String> verifiedSkillsMissingFromResume,
    List<ResumeEvidenceItemDto> resumeEvidence,
    ExperienceAlignmentDto experienceAlignment,
    String aiAssessment,
    List<String> improvementSuggestions,
    BigDecimal confidence
) {
    public static JobResumeAnalysisResponse fromDomain(JobResumeAnalysisResult domain) {
        List<ResumeEvidenceItemDto> evidenceDtos = domain.resumeEvidence().stream()
            .map(e -> new ResumeEvidenceItemDto(
                e.skillOrRequirement(),
                e.section(),
                e.referenceTitle(),
                e.excerpt()
            ))
            .collect(Collectors.toList());

        ExperienceAlignmentDto alignmentDto = null;
        if (domain.experienceAlignment() != null) {
            alignmentDto = new ExperienceAlignmentDto(
                domain.experienceAlignment().candidateYearsExperience(),
                domain.experienceAlignment().requiredMinYears(),
                domain.experienceAlignment().requiredMaxYears(),
                domain.experienceAlignment().meetsExperienceRequirement(),
                domain.experienceAlignment().alignmentNotes()
            );
        }

        return new JobResumeAnalysisResponse(
            domain.resumeId(),
            domain.jobId(),
            domain.resumeName(),
            domain.jobTitle(),
            domain.companyName(),
            domain.strongMatches(),
            domain.missingRequiredSkills(),
            domain.missingPreferredSkills(),
            domain.verifiedSkillsMissingFromResume(),
            evidenceDtos,
            alignmentDto,
            domain.aiAssessment(),
            domain.improvementSuggestions(),
            domain.confidence()
        );
    }
}
