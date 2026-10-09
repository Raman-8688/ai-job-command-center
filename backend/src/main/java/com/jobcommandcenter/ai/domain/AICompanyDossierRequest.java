package com.jobcommandcenter.ai.domain;

import java.util.List;

/**
 * Contextual payload for requesting AI-driven technical company dossier generation.
 */
public record AICompanyDossierRequest(
    String companyName,
    String jobTitle,
    String jobDescription,
    List<String> canonicalJobRequirements,
    List<String> candidateVerifiedSkills,
    List<String> candidateExperiences,
    String companyWebsite,
    String rawCompanyResearch
) {
    public AICompanyDossierRequest {
        if (canonicalJobRequirements == null) {
            canonicalJobRequirements = List.of();
        }
        if (candidateVerifiedSkills == null) {
            candidateVerifiedSkills = List.of();
        }
        if (candidateExperiences == null) {
            candidateExperiences = List.of();
        }
    }
}
