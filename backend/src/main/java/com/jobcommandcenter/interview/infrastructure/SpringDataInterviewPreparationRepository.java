package com.jobcommandcenter.interview.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataInterviewPreparationRepository extends JpaRepository<InterviewPreparationJpaEntity, UUID> {
    List<InterviewPreparationJpaEntity> findByInterviewIdOrderByCreatedAtAsc(UUID interviewId);
    Optional<InterviewPreparationJpaEntity> findByIdAndInterviewId(UUID id, UUID interviewId);
}
