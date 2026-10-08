package com.jobcommandcenter.profile.application;

import com.jobcommandcenter.profile.api.ProfileResponse;
import com.jobcommandcenter.profile.api.UpdateProfileRequest;
import com.jobcommandcenter.profile.domain.Profile;
import com.jobcommandcenter.profile.domain.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional
    public ProfileResponse getProfileByUserId(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.info("Creating initial profile for user: {}", userId);
                return profileRepository.save(Profile.createNew(userId));
            });

        return toResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> Profile.createNew(userId));

        profile.updateDetails(
            request.phone(),
            request.location(),
            request.linkedInUrl(),
            request.gitHubUrl(),
            request.portfolioUrl(),
            request.targetRoles(),
            request.preferredLocations(),
            request.workPreference(),
            request.yearsExperience() != null ? request.yearsExperience() : BigDecimal.ZERO,
            request.noticePeriodDays() != null ? request.noticePeriodDays() : 0,
            request.currentCompany(),
            request.currentDesignation()
        );

        Profile saved = profileRepository.save(profile);
        log.info("Updated professional profile for user: {}", userId);
        return toResponse(saved);
    }

    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getUserId(),
            profile.getPhone(),
            profile.getLocation(),
            profile.getLinkedInUrl(),
            profile.getGitHubUrl(),
            profile.getPortfolioUrl(),
            profile.getTargetRoles(),
            profile.getPreferredLocations(),
            profile.getWorkPreference(),
            profile.getYearsExperience(),
            profile.getNoticePeriodDays(),
            profile.getCurrentCompany(),
            profile.getCurrentDesignation(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
