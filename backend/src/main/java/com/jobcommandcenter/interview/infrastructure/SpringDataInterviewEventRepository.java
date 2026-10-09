package com.jobcommandcenter.interview.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataInterviewEventRepository extends JpaRepository<InterviewEventJpaEntity, UUID> {
    List<InterviewEventJpaEntity> findByInterviewIdOrderByOccurredAtAsc(UUID interviewId);
}
