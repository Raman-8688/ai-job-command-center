package com.jobcommandcenter.email.infrastructure.persistence;

import com.jobcommandcenter.email.domain.Email;
import com.jobcommandcenter.email.domain.EmailRepository;
import com.jobcommandcenter.email.domain.EmailSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EmailPersistenceAdapter implements EmailRepository {

    private final SpringDataEmailRepository springDataRepository;

    public EmailPersistenceAdapter(SpringDataEmailRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Email> findById(UUID id) {
        return springDataRepository.findById(id).map(EmailJpaEntity::toDomain);
    }

    @Override
    public Optional<Email> findByUserIdAndExternalMessageId(UUID userId, String externalMessageId) {
        return springDataRepository.findByUserIdAndExternalMessageId(userId, externalMessageId)
            .map(EmailJpaEntity::toDomain);
    }

    @Override
    public List<Email> search(EmailSearchCriteria criteria) {
        Specification<EmailJpaEntity> spec = buildSpecification(criteria);
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), Sort.by(Sort.Direction.DESC, "receivedAt"));
        return springDataRepository.findAll(spec, pageable)
            .getContent()
            .stream()
            .map(EmailJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long count(EmailSearchCriteria criteria) {
        Specification<EmailJpaEntity> spec = buildSpecification(criteria);
        return springDataRepository.count(spec);
    }

    @Override
    public List<Email> findByUserIdAndAssociatedJobId(UUID userId, UUID jobId) {
        return springDataRepository.findByUserIdAndAssociatedJobIdOrderByReceivedAtDesc(userId, jobId)
            .stream()
            .map(EmailJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Email save(Email email) {
        EmailJpaEntity entity = EmailJpaEntity.fromDomain(email);
        EmailJpaEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<Email> saveAll(List<Email> emails) {
        List<EmailJpaEntity> entities = emails.stream()
            .map(EmailJpaEntity::fromDomain)
            .collect(Collectors.toList());
        return springDataRepository.saveAll(entities)
            .stream()
            .map(EmailJpaEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(Email email) {
        springDataRepository.deleteById(email.getId());
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public long countByUserId(UUID userId) {
        return springDataRepository.countByUserId(userId);
    }

    private Specification<EmailJpaEntity> buildSpecification(EmailSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Tenant isolation
            predicates.add(cb.equal(root.get("userId"), criteria.userId()));

            if (criteria.classification() != null) {
                predicates.add(cb.equal(root.get("classification"), criteria.classification()));
            }

            if (criteria.processingStatus() != null) {
                predicates.add(cb.equal(root.get("processingStatus"), criteria.processingStatus()));
            }

            if (criteria.associatedJobId() != null) {
                predicates.add(cb.equal(root.get("associatedJobId"), criteria.associatedJobId()));
            }

            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().trim().toLowerCase(Locale.ROOT) + "%";
                Predicate subjectMatch = cb.like(cb.lower(root.get("subject")), pattern);
                Predicate senderMatch = cb.like(cb.lower(root.get("sender")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("extractedCompanyName")), pattern);
                predicates.add(cb.or(subjectMatch, senderMatch, companyMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
