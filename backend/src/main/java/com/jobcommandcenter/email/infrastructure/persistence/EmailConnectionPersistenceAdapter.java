package com.jobcommandcenter.email.infrastructure.persistence;

import com.jobcommandcenter.email.domain.EmailConnection;
import com.jobcommandcenter.email.domain.EmailConnectionRepository;
import com.jobcommandcenter.email.domain.EmailProviderType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class EmailConnectionPersistenceAdapter implements EmailConnectionRepository {

    private final SpringDataEmailConnectionRepository springDataRepository;

    public EmailConnectionPersistenceAdapter(SpringDataEmailConnectionRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<EmailConnection> findById(UUID id) {
        return springDataRepository.findById(id).map(EmailConnectionJpaEntity::toDomain);
    }

    @Override
    public Optional<EmailConnection> findByUserIdAndProvider(UUID userId, EmailProviderType provider) {
        return springDataRepository.findByUserIdAndProvider(userId, provider)
            .map(EmailConnectionJpaEntity::toDomain);
    }

    @Override
    public EmailConnection save(EmailConnection connection) {
        EmailConnectionJpaEntity entity = EmailConnectionJpaEntity.fromDomain(connection);
        EmailConnectionJpaEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(EmailConnection connection) {
        springDataRepository.deleteById(connection.getId());
    }

    @Override
    public boolean existsByUserIdAndProvider(UUID userId, EmailProviderType provider) {
        return springDataRepository.existsByUserIdAndProvider(userId, provider);
    }
}
