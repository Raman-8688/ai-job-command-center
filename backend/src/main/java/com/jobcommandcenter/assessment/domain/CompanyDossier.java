package com.jobcommandcenter.assessment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an executive technical intelligence dossier
 * for a target hiring company and specific job role.
 */
public class CompanyDossier {

    private final UUID id;
    private final UUID userId;
    private final UUID jobId;
    private String companyName;
    private String companyTier;
    private String overview;
    private String engineeringScale;
    private String coreTechStack;
    private String engineeringCulture;
    private String architectureFocus;
    private String tailoredTalkingPoints;
    private String interviewerQuestions;
    private final Instant createdAt;
    private Instant updatedAt;

    public CompanyDossier(UUID id, UUID userId, UUID jobId, String companyName, String companyTier,
                          String overview, String engineeringScale, String coreTechStack,
                          String engineeringCulture, String architectureFocus,
                          String tailoredTalkingPoints, String interviewerQuestions,
                          Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Dossier id must not be null");
        this.userId = Objects.requireNonNull(userId, "User id must not be null");
        this.jobId = Objects.requireNonNull(jobId, "Job id must not be null");
        this.companyName = Objects.requireNonNull(companyName, "Company name must not be null");
        this.companyTier = (companyTier != null && !companyTier.isBlank()) ? companyTier : "GROWTH";
        this.overview = overview;
        this.engineeringScale = engineeringScale;
        this.coreTechStack = coreTechStack;
        this.engineeringCulture = engineeringCulture;
        this.architectureFocus = architectureFocus;
        this.tailoredTalkingPoints = tailoredTalkingPoints;
        this.interviewerQuestions = interviewerQuestions;
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
    }

    public static CompanyDossier create(UUID userId, UUID jobId, String companyName, String companyTier,
                                        String overview, String engineeringScale, String coreTechStack,
                                        String engineeringCulture, String architectureFocus,
                                        String tailoredTalkingPoints, String interviewerQuestions) {
        Instant now = Instant.now();
        return new CompanyDossier(
            UUID.randomUUID(),
            userId,
            jobId,
            companyName,
            companyTier,
            overview,
            engineeringScale,
            coreTechStack,
            engineeringCulture,
            architectureFocus,
            tailoredTalkingPoints,
            interviewerQuestions,
            now,
            now
        );
    }

    public void updateBriefing(String overview, String engineeringScale, String coreTechStack,
                               String engineeringCulture, String architectureFocus,
                               String tailoredTalkingPoints, String interviewerQuestions) {
        updateBriefing(null, overview, engineeringScale, coreTechStack, engineeringCulture, architectureFocus, tailoredTalkingPoints, interviewerQuestions);
    }

    public void updateBriefing(String companyTier, String overview, String engineeringScale, String coreTechStack,
                               String engineeringCulture, String architectureFocus,
                               String tailoredTalkingPoints, String interviewerQuestions) {
        if (companyTier != null && !companyTier.isBlank()) {
            this.companyTier = companyTier;
        }
        this.overview = overview;
        this.engineeringScale = engineeringScale;
        this.coreTechStack = coreTechStack;
        this.engineeringCulture = engineeringCulture;
        this.architectureFocus = architectureFocus;
        this.tailoredTalkingPoints = tailoredTalkingPoints;
        this.interviewerQuestions = interviewerQuestions;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getJobId() { return jobId; }
    public String getCompanyName() { return companyName; }
    public String getCompanyTier() { return companyTier; }
    public String getOverview() { return overview; }
    public String getEngineeringScale() { return engineeringScale; }
    public String getCoreTechStack() { return coreTechStack; }
    public String getEngineeringCulture() { return engineeringCulture; }
    public String getArchitectureFocus() { return architectureFocus; }
    public String getTailoredTalkingPoints() { return tailoredTalkingPoints; }
    public String getInterviewerQuestions() { return interviewerQuestions; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
