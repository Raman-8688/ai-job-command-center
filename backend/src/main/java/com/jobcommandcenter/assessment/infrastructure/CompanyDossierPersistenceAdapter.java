package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.CompanyDossier;
import com.jobcommandcenter.assessment.domain.CompanyDossierRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class CompanyDossierPersistenceAdapter implements CompanyDossierRepository {

    private final SpringDataCompanyDossierRepository dossierRepository;

    public CompanyDossierPersistenceAdapter(SpringDataCompanyDossierRepository dossierRepository) {
        this.dossierRepository = dossierRepository;
    }

    @Override
    @Transactional
    public CompanyDossier save(CompanyDossier dossier) {
        CompanyDossierJpaEntity entity = CompanyDossierJpaEntity.fromDomain(dossier);
        CompanyDossierJpaEntity saved = dossierRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<CompanyDossier> findByUserIdAndJobId(UUID userId, UUID jobId) {
        return dossierRepository.findByUserIdAndJobId(userId, jobId)
            .map(CompanyDossierJpaEntity::toDomain);
    }

    @Override
    public Optional<CompanyDossier> findByIdAndUserId(UUID id, UUID userId) {
        return dossierRepository.findByIdAndUserId(id, userId)
            .map(CompanyDossierJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(CompanyDossier dossier) {
        dossierRepository.deleteById(dossier.getId());
    }
}
