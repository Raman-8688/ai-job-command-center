package com.jobcommandcenter.assessment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCompanyDossierRepository extends JpaRepository<CompanyDossierJpaEntity, UUID> {

    Optional<CompanyDossierJpaEntity> findByUserIdAndJobId(UUID userId, UUID jobId);

    Optional<CompanyDossierJpaEntity> findByIdAndUserId(UUID id, UUID userId);
}
