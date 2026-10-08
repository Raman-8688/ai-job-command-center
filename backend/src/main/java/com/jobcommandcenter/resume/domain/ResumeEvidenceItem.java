package com.jobcommandcenter.resume.domain;

/**
 * Value object representing concrete evidence from a resume supporting a skill or requirement.
 */
public record ResumeEvidenceItem(
    String skillOrRequirement,
    String section,
    String referenceTitle,
    String excerpt
) {}
