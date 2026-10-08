package com.jobcommandcenter.job.infrastructure;

import com.jobcommandcenter.job.domain.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JobPersistenceAdapter implements JobRepository {

    private final SpringDataJobRepository springDataJobRepository;

    public JobPersistenceAdapter(SpringDataJobRepository springDataJobRepository) {
        this.springDataJobRepository = springDataJobRepository;
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return springDataJobRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Job> findBySourceAndExternalJobId(JobSource source, String externalJobId) {
        return springDataJobRepository.findBySourceAndExternalJobId(source, externalJobId).map(this::toDomain);
    }

    @Override
    public Optional<Job> findByJobUrl(String jobUrl) {
        return springDataJobRepository.findByJobUrl(jobUrl).map(this::toDomain);
    }

    @Override
    public Optional<Job> findByDeduplicationHash(String deduplicationHash) {
        return springDataJobRepository.findByDeduplicationHash(deduplicationHash).map(this::toDomain);
    }

    @Override
    public List<Job> findAll() {
        return springDataJobRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Job> search(JobSearchCriteria criteria, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<JobJpaEntity> spec = createSpecification(criteria);
        Page<JobJpaEntity> entityPage = springDataJobRepository.findAll(spec, pageRequest);
        return entityPage.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count(JobSearchCriteria criteria) {
        Specification<JobJpaEntity> spec = createSpecification(criteria);
        return springDataJobRepository.count(spec);
    }

    @Override
    public Job save(Job job) {
        JobJpaEntity entity = toEntity(job);
        JobJpaEntity saved = springDataJobRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataJobRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataJobRepository.existsById(id);
    }

    @Override
    public boolean existsByDeduplicationHash(String deduplicationHash) {
        return springDataJobRepository.existsByDeduplicationHash(deduplicationHash);
    }

    @Override
    public boolean existsBySourceAndExternalJobId(JobSource source, String externalJobId) {
        return springDataJobRepository.existsBySourceAndExternalJobId(source, externalJobId);
    }

    @Override
    public boolean existsByJobUrl(String jobUrl) {
        return springDataJobRepository.existsByJobUrl(jobUrl);
    }

    private Specification<JobJpaEntity> createSpecification(JobSearchCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate companyLike = cb.like(cb.lower(root.get("companyName")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, companyLike, descLike));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            if (criteria.source() != null) {
                predicates.add(cb.equal(root.get("source"), criteria.source()));
            }

            if (criteria.workMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), criteria.workMode()));
            }

            if (criteria.companyName() != null && !criteria.companyName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("companyName")), "%" + criteria.companyName().trim().toLowerCase() + "%"));
            }

            if (criteria.location() != null && !criteria.location().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + criteria.location().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Job toDomain(JobJpaEntity entity) {
        List<JobSkill> domainSkills = entity.getSkills() != null
            ? entity.getSkills().stream().map(this::toSkillDomain).collect(Collectors.toList())
            : new ArrayList<>();

        return new Job(
            entity.getId(),
            entity.getExternalJobId(),
            entity.getTitle(),
            entity.getCompanyName(),
            entity.getCompanyWebsite(),
            entity.getJobUrl(),
            entity.getDescription(),
            entity.getLocation(),
            entity.getWorkMode(),
            entity.getEmploymentType(),
            entity.getExperienceMinYears(),
            entity.getExperienceMaxYears(),
            entity.getSalaryMin(),
            entity.getSalaryMax(),
            entity.getSalaryCurrency(),
            entity.getSource(),
            entity.getSourceUrl(),
            entity.getPostedAt(),
            entity.getDiscoveredAt(),
            entity.getApplicationDeadline(),
            entity.getStatus(),
            entity.getDeduplicationHash(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            domainSkills
        );
    }

    private JobJpaEntity toEntity(Job job) {
        List<JobSkillJpaEntity> skillEntities = job.getJobSkills() != null
            ? job.getJobSkills().stream().map(s -> toSkillEntity(s, job.getId())).collect(Collectors.toList())
            : new ArrayList<>();

        return new JobJpaEntity(
            job.getId(),
            job.getExternalJobId(),
            job.getTitle(),
            job.getCompanyName(),
            job.getCompanyWebsite(),
            job.getJobUrl(),
            job.getDescription(),
            job.getLocation(),
            job.getWorkMode(),
            job.getEmploymentType(),
            job.getExperienceMinYears(),
            job.getExperienceMaxYears(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getSalaryCurrency(),
            job.getSource(),
            job.getSourceUrl(),
            job.getPostedAt(),
            job.getDiscoveredAt(),
            job.getApplicationDeadline(),
            job.getStatus(),
            job.getDeduplicationHash(),
            job.getCreatedAt(),
            job.getUpdatedAt(),
            skillEntities
        );
    }

    private JobSkill toSkillDomain(JobSkillJpaEntity entity) {
        return new JobSkill(
            entity.getId(),
            entity.getJobId(),
            entity.getSkillId(),
            entity.getRequirementType(),
            entity.getYearsExperienceRequired(),
            entity.getCreatedAt()
        );
    }

    private JobSkillJpaEntity toSkillEntity(JobSkill domain, UUID jobId) {
        return new JobSkillJpaEntity(
            domain.getId(),
            jobId,
            domain.getSkillId(),
            domain.getRequirementType(),
            domain.getYearsExperienceRequired(),
            domain.getCreatedAt()
        );
    }
}
