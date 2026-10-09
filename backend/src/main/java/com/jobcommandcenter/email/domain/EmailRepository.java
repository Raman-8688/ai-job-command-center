package com.jobcommandcenter.email.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for Email persistence and querying operations.
 */
public interface EmailRepository {

    Optional<Email> findById(UUID id);

    Optional<Email> findByUserIdAndExternalMessageId(UUID userId, String externalMessageId);

    List<Email> search(EmailSearchCriteria criteria);

    long count(EmailSearchCriteria criteria);

    List<Email> findByUserIdAndAssociatedJobId(UUID userId, UUID jobId);

    Email save(Email email);

    List<Email> saveAll(List<Email> emails);

    void delete(Email email);

    void deleteById(UUID id);

    long countByUserId(UUID userId);
}
