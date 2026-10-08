package com.jobcommandcenter.resume.infrastructure;

import com.jobcommandcenter.resume.domain.ResumeStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes")
public class ResumeJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "years_of_experience", precision = 4, scale = 1)
    private BigDecimal yearsOfExperience;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ResumeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ResumeExperienceJpaEntity> experiences = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ResumeProjectJpaEntity> projects = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private List<ResumeSkillJpaEntity> skills = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ResumeEducationJpaEntity> education = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @OrderBy("displayOrder ASC")
    private List<ResumeCertificationJpaEntity> certifications = new ArrayList<>();

    public ResumeJpaEntity() {}

    public ResumeJpaEntity(UUID id,
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
                          List<ResumeExperienceJpaEntity> experiences,
                          List<ResumeProjectJpaEntity> projects,
                          List<ResumeSkillJpaEntity> skills,
                          List<ResumeEducationJpaEntity> education,
                          List<ResumeCertificationJpaEntity> certifications) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.title = title;
        this.summary = summary;
        this.yearsOfExperience = yearsOfExperience;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.experiences = experiences != null ? experiences : new ArrayList<>();
        this.projects = projects != null ? projects : new ArrayList<>();
        this.skills = skills != null ? skills : new ArrayList<>();
        this.education = education != null ? education : new ArrayList<>();
        this.certifications = certifications != null ? certifications : new ArrayList<>();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public BigDecimal getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(BigDecimal yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public ResumeStatus getStatus() { return status; }
    public void setStatus(ResumeStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ResumeExperienceJpaEntity> getExperiences() { return experiences; }
    public void setExperiences(List<ResumeExperienceJpaEntity> experiences) { this.experiences = experiences; }

    public List<ResumeProjectJpaEntity> getProjects() { return projects; }
    public void setProjects(List<ResumeProjectJpaEntity> projects) { this.projects = projects; }

    public List<ResumeSkillJpaEntity> getSkills() { return skills; }
    public void setSkills(List<ResumeSkillJpaEntity> skills) { this.skills = skills; }

    public List<ResumeEducationJpaEntity> getEducation() { return education; }
    public void setEducation(List<ResumeEducationJpaEntity> education) { this.education = education; }

    public List<ResumeCertificationJpaEntity> getCertifications() { return certifications; }
    public void setCertifications(List<ResumeCertificationJpaEntity> certifications) { this.certifications = certifications; }
}
