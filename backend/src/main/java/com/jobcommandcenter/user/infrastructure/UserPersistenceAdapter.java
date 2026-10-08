package com.jobcommandcenter.user.infrastructure;

import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    public UserPersistenceAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(User.normalizeEmail(email)).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(User.normalizeEmail(email));
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return toDomain(saved);
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
            entity.getId(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getDisplayName(),
            entity.getAccountStatus(),
            entity.getRole(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getLastLoginAt()
        );
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getFirstName(),
            user.getLastName(),
            user.getDisplayName(),
            user.getAccountStatus(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoginAt()
        );
    }
}
