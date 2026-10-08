package com.jobcommandcenter.profile.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Domain entity representing a candidate's professional profile.
 * Maintained separately from User identity.
 */
public class Profile {

    private final UUID id;
    private final UUID userId;
    private String phone;
    private String location;
    private String linkedInUrl;
    private String gitHubUrl;
    private String portfolioUrl;
    private List<String> targetRoles;
    private List<String> preferredLocations;
    private WorkPreference workPreference;
    private BigDecimal yearsExperience;
    private int noticePeriodDays;
    private String currentCompany;
    private String currentDesignation;
    private final Instant createdAt;
    private Instant updatedAt;

    public Profile(UUID id,
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
        this.id = Objects.requireNonNull(id, "Profile ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.phone = phone;
        this.location = location;
        this.linkedInUrl = linkedInUrl;
        this.gitHubUrl = gitHubUrl;
        this.portfolioUrl = portfolioUrl;
        this.targetRoles = targetRoles != null ? new ArrayList<>(targetRoles) : new ArrayList<>();
        this.preferredLocations = preferredLocations != null ? new ArrayList<>(preferredLocations) : new ArrayList<>();
        this.workPreference = workPreference != null ? workPreference : WorkPreference.REMOTE;
        this.yearsExperience = yearsExperience != null ? yearsExperience : BigDecimal.ZERO;
        this.noticePeriodDays = Math.max(0, noticePeriodDays);
        this.currentCompany = currentCompany;
        this.currentDesignation = currentDesignation;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Profile createNew(UUID userId) {
        Instant now = Instant.now();
        return new Profile(
            UUID.randomUUID(),
            userId,
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            WorkPreference.REMOTE,
            BigDecimal.ZERO,
            0,
            null,
            null,
            now,
            now
        );
    }

    public void updateDetails(String phone,
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
                              String currentDesignation) {
        this.phone = phone;
        this.location = location;
        this.linkedInUrl = linkedInUrl;
        this.gitHubUrl = gitHubUrl;
        this.portfolioUrl = portfolioUrl;
        this.targetRoles = targetRoles != null ? new ArrayList<>(targetRoles) : new ArrayList<>();
        this.preferredLocations = preferredLocations != null ? new ArrayList<>(preferredLocations) : new ArrayList<>();
        this.workPreference = workPreference != null ? workPreference : WorkPreference.REMOTE;
        this.yearsExperience = yearsExperience != null ? yearsExperience : BigDecimal.ZERO;
        this.noticePeriodDays = Math.max(0, noticePeriodDays);
        this.currentCompany = currentCompany;
        this.currentDesignation = currentDesignation;
        this.updatedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getLinkedInUrl() { return linkedInUrl; }
    public String getGitHubUrl() { return gitHubUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public List<String> getTargetRoles() { return Collections.unmodifiableList(targetRoles); }
    public List<String> getPreferredLocations() { return Collections.unmodifiableList(preferredLocations); }
    public WorkPreference getWorkPreference() { return workPreference; }
    public BigDecimal getYearsExperience() { return yearsExperience; }
    public int getNoticePeriodDays() { return noticePeriodDays; }
    public String getCurrentCompany() { return currentCompany; }
    public String getCurrentDesignation() { return currentDesignation; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
