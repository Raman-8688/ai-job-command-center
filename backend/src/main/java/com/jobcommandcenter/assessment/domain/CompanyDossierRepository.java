package com.jobcommandcenter.assessment.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for managing CompanyDossier entities.
 */
public interface CompanyDossierRepository {

    CompanyDossier save(CompanyDossier dossier);

    Optional<CompanyDossier> findByUserIdAndJobId(UUID userId, UUID jobId);

    Optional<CompanyDossier> findByIdAndUserId(UUID id, UUID userId);

    void delete(CompanyDossier dossier);
}
