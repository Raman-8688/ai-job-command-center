package com.jobcommandcenter.ai.infrastructure;

import com.jobcommandcenter.ai.domain.AnalyzedTechnology;
import com.jobcommandcenter.ai.domain.JobAiAnalysis;
import com.jobcommandcenter.ai.domain.JobAiAnalysisRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JobAiAnalysisPersistenceAdapter implements JobAiAnalysisRepository {

    private final SpringDataJobAiAnalysisRepository repository;

    public JobAiAnalysisPersistenceAdapter(SpringDataJobAiAnalysisRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobAiAnalysis> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobAiAnalysis> findLatestByJobId(UUID jobId) {
        return repository.findFirstByJobIdOrderByVersionDesc(jobId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobAiAnalysis> findAllByJobIdOrderByVersionDesc(UUID jobId) {
        return repository.findAllByJobIdOrderByVersionDesc(jobId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int findMaxVersionByJobId(UUID jobId) {
        return repository.findMaxVersionByJobId(jobId);
    }

    @Override
    @Transactional
    public JobAiAnalysis save(JobAiAnalysis analysis) {
        JobAiAnalysisJpaEntity entity = toEntity(analysis);
        JobAiAnalysisJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByJobId(UUID jobId) {
        return repository.existsByJobId(jobId);
    }

    private JobAiAnalysis toDomain(JobAiAnalysisJpaEntity entity) {
        List<String> coreResponsibilities = new ArrayList<>();
        List<String> inferredResponsibilities = new ArrayList<>();

        if (entity.getResponsibilities() != null) {
            for (JobAiResponsibilityEmbeddable r : entity.getResponsibilities()) {
                if (r.isInferred()) {
                    inferredResponsibilities.add(r.getResponsibility());
                } else {
                    coreResponsibilities.add(r.getResponsibility());
                }
            }
        }

        List<AnalyzedTechnology> technologies = new ArrayList<>();
        if (entity.getTechnologies() != null) {
            for (JobAiTechnologyEmbeddable t : entity.getTechnologies()) {
                technologies.add(new AnalyzedTechnology(t.getTechnology(), t.getCategory(), t.isRequired()));
            }
        }

        List<String> educationRequirements = new ArrayList<>();
        List<String> certificationRequirements = new ArrayList<>();
        if (entity.getRequirements() != null) {
            for (JobAiRequirementEmbeddable req : entity.getRequirements()) {
                if ("CERTIFICATION".equalsIgnoreCase(req.getRequirementType())) {
                    certificationRequirements.add(req.getRequirement());
                } else {
                    educationRequirements.add(req.getRequirement());
                }
            }
        }

        List<String> potentialRedFlags = entity.getRedFlags() != null
            ? new ArrayList<>(entity.getRedFlags())
            : new ArrayList<>();

        return new JobAiAnalysis(
            entity.getId(),
            entity.getJobId(),
            entity.getVersion(),
            entity.getProvider(),
            entity.getModel(),
            entity.getPromptVersion(),
            entity.getStatus(),
            entity.getNormalizedTitle(),
            entity.getSeniorityLevel(),
            coreResponsibilities,
            inferredResponsibilities,
            technologies,
            educationRequirements,
            certificationRequirements,
            entity.getExperienceExpectations(),
            potentialRedFlags,
            entity.getConfidence(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getCompletedAt()
        );
    }

    private JobAiAnalysisJpaEntity toEntity(JobAiAnalysis domain) {
        Set<JobAiResponsibilityEmbeddable> responsibilities = new HashSet<>();
        for (String resp : domain.getCoreResponsibilities()) {
            responsibilities.add(new JobAiResponsibilityEmbeddable(resp, false));
        }
        for (String resp : domain.getInferredResponsibilities()) {
            responsibilities.add(new JobAiResponsibilityEmbeddable(resp, true));
        }

        Set<JobAiTechnologyEmbeddable> technologies = new HashSet<>();
        for (AnalyzedTechnology tech : domain.getTechnologies()) {
            technologies.add(new JobAiTechnologyEmbeddable(tech.category(), tech.technology(), tech.required()));
        }

        Set<JobAiRequirementEmbeddable> requirements = new HashSet<>();
        for (String edu : domain.getEducationRequirements()) {
            requirements.add(new JobAiRequirementEmbeddable("EDUCATION", edu));
        }
        for (String cert : domain.getCertificationRequirements()) {
            requirements.add(new JobAiRequirementEmbeddable("CERTIFICATION", cert));
        }

        Set<String> redFlags = new HashSet<>(domain.getPotentialRedFlags());

        return new JobAiAnalysisJpaEntity(
            domain.getId(),
            domain.getJobId(),
            domain.getVersion(),
            domain.getProvider(),
            domain.getModel(),
            domain.getPromptVersion(),
            domain.getStatus(),
            domain.getNormalizedTitle(),
            domain.getSeniorityLevel(),
            domain.getExperienceExpectations(),
            domain.getConfidence(),
            domain.getErrorMessage(),
            domain.getCreatedAt(),
            domain.getCompletedAt(),
            responsibilities,
            technologies,
            requirements,
            redFlags
        );
    }
}
