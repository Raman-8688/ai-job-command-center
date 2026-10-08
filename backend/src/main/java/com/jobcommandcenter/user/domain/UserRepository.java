package com.jobcommandcenter.user.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository abstraction for User management.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
