package com.jobcommandcenter.resume.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository contract for Resume aggregate root.
 */
public interface ResumeRepository {

    Optional<Resume> findById(UUID id);

    List<Resume> findByUserId(UUID userId);

    Optional<Resume> findByIdAndUserId(UUID id, UUID userId);

    Resume save(Resume resume);

    void deleteById(UUID id);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    boolean existsById(UUID id);
}
