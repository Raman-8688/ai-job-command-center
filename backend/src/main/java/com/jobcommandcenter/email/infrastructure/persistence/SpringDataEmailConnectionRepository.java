package com.jobcommandcenter.email.infrastructure.persistence;

import com.jobcommandcenter.email.domain.EmailProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEmailConnectionRepository extends JpaRepository<EmailConnectionJpaEntity, UUID> {

    Optional<EmailConnectionJpaEntity> findByUserIdAndProvider(UUID userId, EmailProviderType provider);

    boolean existsByUserIdAndProvider(UUID userId, EmailProviderType provider);
}
