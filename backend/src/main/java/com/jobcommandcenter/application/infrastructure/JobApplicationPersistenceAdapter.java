package com.jobcommandcenter.application.infrastructure;

import com.jobcommandcenter.application.domain.ApplicationStatus;
import com.jobcommandcenter.application.domain.JobApplication;
import com.jobcommandcenter.application.domain.JobApplicationEvent;
import com.jobcommandcenter.application.domain.JobApplicationRepository;
import com.jobcommandcenter.application.domain.JobApplicationSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JobApplicationPersistenceAdapter implements JobApplicationRepository {

    private final SpringDataJobApplicationRepository repository;

    public JobApplicationPersistenceAdapter(SpringDataJobApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobApplication> findById(UUID id) {
        return repository.findById(id).map(JobApplicationJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobApplication> findByUserIdAndJobId(UUID userId, UUID jobId) {
        return repository.findByUserIdAndJobId(userId, jobId).map(JobApplicationJpaEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndJobId(UUID userId, UUID jobId) {
        return repository.existsByUserIdAndJobId(userId, jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplication> search(JobApplicationSearchCriteria criteria) {
        Specification<JobApplicationJpaEntity> spec = buildSpecification(criteria);
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), Sort.by(Sort.Direction.DESC, "updatedAt"));
        return repository.findAll(spec, pageable)
            .getContent()
            .stream()
            .map(JobApplicationJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count(JobApplicationSearchCriteria criteria) {
        Specification<JobApplicationJpaEntity> spec = buildSpecification(criteria);
        return repository.count(spec);
    }

    @Override
    @Transactional
    public JobApplication save(JobApplication application) {
        JobApplicationJpaEntity entity = repository.findById(application.getId())
            .orElseGet(JobApplicationJpaEntity::new);

        entity.setId(application.getId());
        entity.setUserId(application.getUserId());
        entity.setJobId(application.getJobId());
        entity.setResumeId(application.getResumeId());
        entity.setTailoredResumeId(application.getTailoredResumeId());
        entity.setStatus(application.getStatus());
        entity.setAppliedAt(application.getAppliedAt());
        entity.setSubmissionSource(application.getSubmissionSource());
        entity.setExternalReference(application.getExternalReference());
        entity.setNextFollowUpDate(application.getNextFollowUpDate());
        entity.setNotes(application.getNotes());
        entity.setCreatedAt(application.getCreatedAt());
        entity.setUpdatedAt(application.getUpdatedAt());
        entity.setVersion(application.getVersion());

        if (application.getEvents() != null) {
            Map<UUID, JobApplicationEventJpaEntity> existingEvents = entity.getEvents().stream()
                .collect(Collectors.toMap(JobApplicationEventJpaEntity::getId, e -> e));

            List<JobApplicationEventJpaEntity> updatedEvents = new ArrayList<>();
            for (JobApplicationEvent domainEvent : application.getEvents()) {
                JobApplicationEventJpaEntity eventEntity = existingEvents.get(domainEvent.getId());
                if (eventEntity == null) {
                    eventEntity = JobApplicationEventJpaEntity.fromDomain(domainEvent);
                }
                updatedEvents.add(eventEntity);
            }
            entity.getEvents().clear();
            entity.getEvents().addAll(updatedEvents);
        }

        JobApplicationJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void delete(JobApplication application) {
        repository.deleteById(application.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> countByStatusForUser(UUID userId) {
        List<Object[]> results = repository.countByStatusForUser(userId);
        Map<ApplicationStatus, Long> map = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus s : ApplicationStatus.values()) {
            map.put(s, 0L);
        }
        for (Object[] row : results) {
            if (row[0] instanceof ApplicationStatus status && row[1] instanceof Number count) {
                map.put(status, count.longValue());
            }
        }
        return map;
    }

    @Override
    @Transactional(readOnly = true)
    public long countFollowUpsDueForUser(UUID userId) {
        List<ApplicationStatus> closed = List.of(
            ApplicationStatus.OFFER,
            ApplicationStatus.ACCEPTED,
            ApplicationStatus.REJECTED,
            ApplicationStatus.WITHDRAWN,
            ApplicationStatus.ARCHIVED
        );
        return repository.countFollowUpsDueForUser(userId, Instant.now(), closed);
    }

    private Specification<JobApplicationJpaEntity> buildSpecification(JobApplicationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.userId() != null) {
                predicates.add(cb.equal(root.get("userId"), criteria.userId()));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String pattern = "%" + criteria.search().trim().toLowerCase(Locale.ROOT) + "%";
                Predicate refMatch = cb.like(cb.lower(root.get("externalReference")), pattern);
                Predicate notesMatch = cb.like(cb.lower(root.get("notes")), pattern);
                predicates.add(cb.or(refMatch, notesMatch));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
