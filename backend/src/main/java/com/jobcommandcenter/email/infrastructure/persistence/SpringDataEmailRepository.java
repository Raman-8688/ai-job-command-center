package com.jobcommandcenter.email.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEmailRepository extends JpaRepository<EmailJpaEntity, UUID>, JpaSpecificationExecutor<EmailJpaEntity> {

    Optional<EmailJpaEntity> findByUserIdAndExternalMessageId(UUID userId, String externalMessageId);

    List<EmailJpaEntity> findByUserIdAndAssociatedJobIdOrderByReceivedAtDesc(UUID userId, UUID associatedJobId);

    long countByUserId(UUID userId);
}
