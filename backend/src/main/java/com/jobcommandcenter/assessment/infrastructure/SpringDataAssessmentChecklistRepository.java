package com.jobcommandcenter.assessment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAssessmentChecklistRepository extends JpaRepository<AssessmentChecklistJpaEntity, UUID> {

    Optional<AssessmentChecklistJpaEntity> findByIdAndAssessmentId(UUID id, UUID assessmentId);

    List<AssessmentChecklistJpaEntity> findByAssessmentIdOrderBySortOrderAsc(UUID assessmentId);
}
