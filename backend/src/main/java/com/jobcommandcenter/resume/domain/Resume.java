package com.jobcommandcenter.resume.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate root representing a candidate's structured resume.
 * A candidate may maintain multiple resumes tailored for different roles.
 */
public class Resume {

    private final UUID id;
    private final UUID userId;
    private String name;
    private String title;
    private String summary;
    private BigDecimal yearsOfExperience;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private ResumeStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private List<ResumeExperience> experiences;
    private List<ResumeProject> projects;
    private List<ResumeSkill> skills;
    private List<ResumeEducation> education;
    private List<ResumeCertification> certifications;

    public Resume(UUID id,
                  UUID userId,
                  String name,
                  String title,
                  String summary,
                  BigDecimal yearsOfExperience,
                  String location,
                  String contactEmail,
                  String contactPhone,
                  ResumeStatus status,
                  Instant createdAt,
                  Instant updatedAt,
                  List<ResumeExperience> experiences,
                  List<ResumeProject> projects,
                  List<ResumeSkill> skills,
                  List<ResumeEducation> education,
                  List<ResumeCertification> certifications) {
        this.id = Objects.requireNonNull(id, "Resume ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.name = validateNonBlank(name, "Resume name cannot be blank");
        this.title = title;
        this.summary = summary;
        this.yearsOfExperience = yearsOfExperience != null ? yearsOfExperience : BigDecimal.ZERO;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status != null ? status : ResumeStatus.DRAFT;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.experiences = experiences != null ? new ArrayList<>(experiences) : new ArrayList<>();
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.education = education != null ? new ArrayList<>(education) : new ArrayList<>();
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
    }

    public static Resume create(UUID userId,
                                String name,
                                String title,
                                String summary,
                                BigDecimal yearsOfExperience,
                                String location,
                                String contactEmail,
                                String contactPhone) {
        Instant now = Instant.now();
        return new Resume(
            UUID.randomUUID(),
            userId,
            name,
            title,
            summary,
            yearsOfExperience,
            location,
            contactEmail,
            contactPhone,
            ResumeStatus.DRAFT,
            now,
            now,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    public boolean isOwnedBy(UUID requestingUserId) {
        return this.userId.equals(requestingUserId);
    }

    public void activate() {
        this.status = ResumeStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = ResumeStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public void updateMetadata(String name,
                               String title,
                               String summary,
                               BigDecimal yearsOfExperience,
                               String location,
                               String contactEmail,
                               String contactPhone) {
        this.name = validateNonBlank(name, "Resume name cannot be blank");
        this.title = title;
        this.summary = summary;
        this.yearsOfExperience = yearsOfExperience != null ? yearsOfExperience : BigDecimal.ZERO;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.updatedAt = Instant.now();
    }

    // Section Mutations
    public void setExperiences(List<ResumeExperience> experiences) {
        this.experiences = experiences != null ? new ArrayList<>(experiences) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void setProjects(List<ResumeProject> projects) {
        this.projects = projects != null ? new ArrayList<>(projects) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void setSkills(List<ResumeSkill> skills) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void setEducation(List<ResumeEducation> education) {
        this.education = education != null ? new ArrayList<>(education) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void setCertifications(List<ResumeCertification> certifications) {
        this.certifications = certifications != null ? new ArrayList<>(certifications) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    private static String validateNonBlank(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public BigDecimal getYearsOfExperience() { return yearsOfExperience; }
    public String getLocation() { return location; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public ResumeStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<ResumeExperience> getExperiences() { return Collections.unmodifiableList(experiences); }
    public List<ResumeProject> getProjects() { return Collections.unmodifiableList(projects); }
    public List<ResumeSkill> getSkills() { return Collections.unmodifiableList(skills); }
    public List<ResumeEducation> getEducation() { return Collections.unmodifiableList(education); }
    public List<ResumeCertification> getCertifications() { return Collections.unmodifiableList(certifications); }
}
