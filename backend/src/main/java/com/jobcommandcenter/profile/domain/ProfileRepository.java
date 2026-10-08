package com.jobcommandcenter.profile.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository abstraction for candidate professional profile.
 */
public interface ProfileRepository {

    Optional<Profile> findByUserId(UUID userId);

    Profile save(Profile profile);
}
