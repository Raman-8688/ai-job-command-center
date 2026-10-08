package com.jobcommandcenter.profile.infrastructure;

import com.jobcommandcenter.profile.domain.Profile;
import com.jobcommandcenter.profile.domain.ProfileRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProfilePersistenceAdapter implements ProfileRepository {

    private final SpringDataProfileRepository springDataProfileRepository;

    public ProfilePersistenceAdapter(SpringDataProfileRepository springDataProfileRepository) {
        this.springDataProfileRepository = springDataProfileRepository;
    }

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return springDataProfileRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = toEntity(profile);
        ProfileJpaEntity saved = springDataProfileRepository.save(entity);
        return toDomain(saved);
    }

    private Profile toDomain(ProfileJpaEntity entity) {
        return new Profile(
            entity.getId(),
            entity.getUserId(),
            entity.getPhone(),
            entity.getLocation(),
            entity.getLinkedInUrl(),
            entity.getGitHubUrl(),
            entity.getPortfolioUrl(),
            entity.getTargetRoles(),
            entity.getPreferredLocations(),
            entity.getWorkPreference(),
            entity.getYearsExperience(),
            entity.getNoticePeriodDays(),
            entity.getCurrentCompany(),
            entity.getCurrentDesignation(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private ProfileJpaEntity toEntity(Profile profile) {
        return new ProfileJpaEntity(
            profile.getId(),
            profile.getUserId(),
            profile.getPhone(),
            profile.getLocation(),
            profile.getLinkedInUrl(),
            profile.getGitHubUrl(),
            profile.getPortfolioUrl(),
            new ArrayList<>(profile.getTargetRoles()),
            new ArrayList<>(profile.getPreferredLocations()),
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
