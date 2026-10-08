package com.jobcommandcenter.profile.infrastructure;

import com.jobcommandcenter.profile.domain.WorkPreference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "linkedin_url", length = 500)
    private String linkedInUrl;

    @Column(name = "github_url", length = 500)
    private String gitHubUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_target_roles", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "target_role", nullable = false)
    private List<String> targetRoles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_preferred_locations", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "preferred_location", nullable = false)
    private List<String> preferredLocations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "work_preference", nullable = false, length = 50)
    private WorkPreference workPreference;

    @Column(name = "years_experience", nullable = false, precision = 4, scale = 1)
    private BigDecimal yearsExperience;

    @Column(name = "notice_period_days", nullable = false)
    private int noticePeriodDays;

    @Column(name = "current_company", length = 255)
    private String currentCompany;

    @Column(name = "current_designation", length = 255)
    private String currentDesignation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProfileJpaEntity() {
    }

    public ProfileJpaEntity(UUID id,
                            UUID userId,
                            String phone,
                            String location,
                            String linkedInUrl,
                            String gitHubUrl,
                            String portfolioUrl,
                            List<String> targetRoles,
                            List<String> preferredLocations,
                            WorkPreference workPreference,
                            BigDecimal yearsExperience,
                            int noticePeriodDays,
                            String currentCompany,
                            String currentDesignation,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.phone = phone;
        this.location = location;
        this.linkedInUrl = linkedInUrl;
        this.gitHubUrl = gitHubUrl;
        this.portfolioUrl = portfolioUrl;
        this.targetRoles = targetRoles != null ? targetRoles : new ArrayList<>();
        this.preferredLocations = preferredLocations != null ? preferredLocations : new ArrayList<>();
        this.workPreference = workPreference;
        this.yearsExperience = yearsExperience;
        this.noticePeriodDays = noticePeriodDays;
        this.currentCompany = currentCompany;
        this.currentDesignation = currentDesignation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLinkedInUrl() { return linkedInUrl; }
    public void setLinkedInUrl(String linkedInUrl) { this.linkedInUrl = linkedInUrl; }
    public String getGitHubUrl() { return gitHubUrl; }
    public void setGitHubUrl(String gitHubUrl) { this.gitHubUrl = gitHubUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
    public List<String> getTargetRoles() { return targetRoles; }
    public void setTargetRoles(List<String> targetRoles) { this.targetRoles = targetRoles; }
    public List<String> getPreferredLocations() { return preferredLocations; }
    public void setPreferredLocations(List<String> preferredLocations) { this.preferredLocations = preferredLocations; }
    public WorkPreference getWorkPreference() { return workPreference; }
    public void setWorkPreference(WorkPreference workPreference) { this.workPreference = workPreference; }
    public BigDecimal getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(BigDecimal yearsExperience) { this.yearsExperience = yearsExperience; }
    public int getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(int noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }
    public String getCurrentCompany() { return currentCompany; }
    public void setCurrentCompany(String currentCompany) { this.currentCompany = currentCompany; }
    public String getCurrentDesignation() { return currentDesignation; }
    public void setCurrentDesignation(String currentDesignation) { this.currentDesignation = currentDesignation; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
