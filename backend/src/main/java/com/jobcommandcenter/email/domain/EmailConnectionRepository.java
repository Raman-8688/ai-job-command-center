package com.jobcommandcenter.email.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for EmailConnection persistence operations.
 */
public interface EmailConnectionRepository {

    Optional<EmailConnection> findById(UUID id);

    Optional<EmailConnection> findByUserIdAndProvider(UUID userId, EmailProviderType provider);

    EmailConnection save(EmailConnection connection);

    void delete(EmailConnection connection);

    boolean existsByUserIdAndProvider(UUID userId, EmailProviderType provider);
}
