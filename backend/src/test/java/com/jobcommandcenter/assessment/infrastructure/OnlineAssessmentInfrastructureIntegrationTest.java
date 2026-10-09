package com.jobcommandcenter.assessment.infrastructure;

import com.jobcommandcenter.assessment.domain.*;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.user.domain.Role;
import com.jobcommandcenter.user.domain.User;
import com.jobcommandcenter.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OnlineAssessmentInfrastructureIntegrationTest {

    @Autowired
    private OnlineAssessmentRepository assessmentRepository;

    @Autowired
    private CompanyDossierRepository dossierRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User candidateUser;
    private User otherCandidate;
    private Job testJob1;
    private Job testJob2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM online_assessment_checklists");
        jdbcTemplate.execute("DELETE FROM online_assessment_events");
        jdbcTemplate.execute("DELETE FROM online_assessments");
        jdbcTemplate.execute("DELETE FROM company_dossiers");
        jdbcTemplate.execute("DELETE FROM interview_preparations");
        jdbcTemplate.execute("DELETE FROM interview_events");
        jdbcTemplate.execute("DELETE FROM interviews");
        jdbcTemplate.execute("DELETE FROM job_application_events");
        jdbcTemplate.execute("DELETE FROM job_applications");
        jdbcTemplate.execute("DELETE FROM jobs");
        jdbcTemplate.execute("DELETE FROM users");

        candidateUser = User.createNew(
            "oa.candidate@jobcommandcenter.com",
            passwordEncoder.encode("Secret123!"),
            "Ada",
            "Lovelace",
            "Ada L.",
            Role.USER
        );
        userRepository.save(candidateUser);

        otherCandidate = User.createNew(
            "other.oa@jobcommandcenter.com",
            passwordEncoder.encode("Secret123!"),
            "Grace",
            "Hopper",
            "Grace H.",
            Role.USER
        );
        userRepository.save(otherCandidate);

        testJob1 = Job.createNew(
            "ext-oa-1",
            "Staff Infrastructure Engineer",
            "Datadog",
            "https://datadoghq.com",
            "https://careers.datadoghq.com/1",
            "Build large scale telemetry and distributed data ingestion",
            "New York, NY",
            WorkMode.HYBRID,
            EmploymentType.FULL_TIME,
            new BigDecimal("6.0"),
            new BigDecimal("12.0"),
            new BigDecimal("210000"),
            new BigDecimal("270000"),
            "USD",
            JobSource.LINKEDIN,
            "https://careers.datadoghq.com/1",
            Instant.now(),
            null
        );
        jobRepository.save(testJob1);

        testJob2 = Job.createNew(
            "ext-oa-2",
            "Senior Distributed Systems Engineer",
            "Stripe",
            "https://stripe.com",
            "https://stripe.com/jobs/2",
            "Global ledger and real-time payment consistency",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            new BigDecimal("10.0"),
            new BigDecimal("220000"),
            new BigDecimal("290000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://stripe.com/jobs/2",
            Instant.now(),
            null
        );
        jobRepository.save(testJob2);
    }

    @Test
    @DisplayName("Assessment persistence enforces user-scoped multi-tenant retrieval")
    void persistenceEnforcesUserIsolation() {
        OnlineAssessment assessment = OnlineAssessment.create(
            candidateUser.getId(),
            testJob1.getId(),
            null,
            null,
            AssessmentPlatform.CODESIGNAL,
            "GCA Coding Assessment",
            70,
            Instant.now(),
            Instant.now().plus(2, ChronoUnit.DAYS),
            "https://codesignal.com/test/1",
            "CODE-123"
        );

        OnlineAssessment saved = assessmentRepository.save(assessment);
        assertNotNull(saved.getId());

        // Candidate can retrieve their assessment
        Optional<OnlineAssessment> found = assessmentRepository.findByIdAndUserId(saved.getId(), candidateUser.getId());
        assertTrue(found.isPresent());
        assertEquals("GCA Coding Assessment", found.get().getTitle());

        // Other candidate receives empty Optional (multi-tenant boundary)
        Optional<OnlineAssessment> otherFound = assessmentRepository.findByIdAndUserId(saved.getId(), otherCandidate.getId());
        assertTrue(otherFound.isEmpty());
    }

    @Test
    @DisplayName("Search criteria filters assessments by status, platform, and target job")
    void searchCriteriaFiltering() {
        Instant now = Instant.now();

        OnlineAssessment oa1 = OnlineAssessment.create(
            candidateUser.getId(), testJob1.getId(), null, null, AssessmentPlatform.CODESIGNAL,
            "CodeSignal GCA", 70, now, now.plus(2, ChronoUnit.DAYS), null, null
        );
        assessmentRepository.save(oa1);

        OnlineAssessment oa2 = OnlineAssessment.create(
            candidateUser.getId(), testJob2.getId(), null, null, AssessmentPlatform.HACKERRANK,
            "HackerRank Backend OA", 90, now, now.plus(4, ChronoUnit.DAYS), null, null
        );
        oa2.start(now);
        oa2.submit(new BigDecimal("100"), new BigDecimal("100"), "Passed", null, now.plus(1, ChronoUnit.HOURS));
        assessmentRepository.save(oa2);

        // Filter by platform CODESIGNAL
        List<OnlineAssessment> codeSignalList = assessmentRepository.findByUserId(
            candidateUser.getId(),
            new OnlineAssessmentSearchCriteria(null, AssessmentPlatform.CODESIGNAL, null, null, null, null)
        );
        assertEquals(1, codeSignalList.size());
        assertEquals("CodeSignal GCA", codeSignalList.get(0).getTitle());

        // Filter by status SUBMITTED
        List<OnlineAssessment> submittedList = assessmentRepository.findByUserId(
            candidateUser.getId(),
            new OnlineAssessmentSearchCriteria(AssessmentStatus.SUBMITTED, null, null, null, null, null)
        );
        assertEquals(1, submittedList.size());
        assertEquals("HackerRank Backend OA", submittedList.get(0).getTitle());

        // Filter by Job 1
        List<OnlineAssessment> job1List = assessmentRepository.findByUserId(
            candidateUser.getId(),
            new OnlineAssessmentSearchCriteria(null, null, testJob1.getId(), null, null, null)
        );
        assertEquals(1, job1List.size());
        assertEquals(testJob1.getId(), job1List.get(0).getJobId());
    }

    @Test
    @DisplayName("findActiveByUserId and findDueSoonByUserId query active deadlines correctly")
    void activeAndDueSoonQueries() {
        Instant now = Instant.now();

        // OA 1: Due in 12 hours (within 48 hour window)
        OnlineAssessment oa1 = OnlineAssessment.create(
            candidateUser.getId(), testJob1.getId(), null, null, AssessmentPlatform.CODESIGNAL,
            "Due Soon OA", 70, now, now.plus(12, ChronoUnit.HOURS), null, null
        );
        assessmentRepository.save(oa1);

        // OA 2: Due in 5 days (active, but outside 48 hour window)
        OnlineAssessment oa2 = OnlineAssessment.create(
            candidateUser.getId(), testJob2.getId(), null, null, AssessmentPlatform.HACKERRANK,
            "Due Later OA", 90, now, now.plus(5, ChronoUnit.DAYS), null, null
        );
        assessmentRepository.save(oa2);

        // Active query returns both
        List<OnlineAssessment> active = assessmentRepository.findActiveByUserId(candidateUser.getId());
        assertEquals(2, active.size());

        // Due soon within 48h window returns only OA 1
        Instant windowEnd = now.plus(48, ChronoUnit.HOURS);
        List<OnlineAssessment> dueSoon = assessmentRepository.findDueSoonByUserId(candidateUser.getId(), now, windowEnd);
        assertEquals(1, dueSoon.size());
        assertEquals("Due Soon OA", dueSoon.get(0).getTitle());

        long dueSoonCount = assessmentRepository.countDueSoonByUserId(candidateUser.getId(), now, windowEnd);
        assertEquals(1L, dueSoonCount);
    }

    @Test
    @DisplayName("Event history is persisted append-only and returned in chronological order")
    void eventHistoryAppendOnlyOrdering() {
        Instant now = Instant.now();

        OnlineAssessment oa = OnlineAssessment.create(
            candidateUser.getId(), testJob1.getId(), null, null, AssessmentPlatform.CODESIGNAL,
            "Event Timeline Test", 70, now, now.plus(3, ChronoUnit.DAYS), null, null
        );

        oa.start(now.plus(1, ChronoUnit.HOURS));
        oa.extendDeadline(now.plus(5, ChronoUnit.DAYS), "Recruiter extension");
        oa.submit(new BigDecimal("820"), new BigDecimal("850"), "All tasks solved", null, now.plus(2, ChronoUnit.HOURS));
        oa.recordResult(AssessmentResult.PASSED, "Advanced to round 2");

        assessmentRepository.save(oa);

        OnlineAssessment retrieved = assessmentRepository.findByIdAndUserId(oa.getId(), candidateUser.getId()).orElseThrow();
        List<OnlineAssessmentEvent> events = retrieved.getEvents();

        assertEquals(5, events.size());
        assertEquals(AssessmentEventType.INVITED, events.get(0).getEventType());
        assertEquals(AssessmentEventType.STARTED, events.get(1).getEventType());
        assertEquals(AssessmentEventType.DEADLINE_EXTENDED, events.get(2).getEventType());
        assertEquals(AssessmentEventType.SUBMITTED, events.get(3).getEventType());
        assertEquals(AssessmentEventType.RESULT_RECORDED, events.get(4).getEventType());
    }

    @Test
    @DisplayName("Checklist items persist and update within assessment cascade")
    void checklistPersistenceAndToggle() {
        OnlineAssessment oa = OnlineAssessment.create(
            candidateUser.getId(), testJob1.getId(), null, null, AssessmentPlatform.CODESIGNAL,
            "Checklist Test", 70, Instant.now(), Instant.now().plus(2, ChronoUnit.DAYS), null, null
        );

        AssessmentChecklistItem item1 = AssessmentChecklistItem.create(oa.getId(), "ALGORITHMS", "2D Grid BFS", "Matrix traversal", 1);
        AssessmentChecklistItem item2 = AssessmentChecklistItem.create(oa.getId(), "DATA_STRUCTURES", "Prefix Sum", "Subarray sum", 2);
        oa.addChecklistItems(List.of(item1, item2));

        OnlineAssessment saved = assessmentRepository.save(oa);
        assertEquals(2, saved.getChecklists().size());

        // Toggle via persistence adapter saveChecklistItem
        AssessmentChecklistItem foundItem = assessmentRepository.findChecklistItemById(oa.getId(), item1.getId()).orElseThrow();
        assertFalse(foundItem.isCompleted());

        foundItem.toggleCompleted();
        assessmentRepository.saveChecklistItem(foundItem);

        AssessmentChecklistItem updatedItem = assessmentRepository.findChecklistItemById(oa.getId(), item1.getId()).orElseThrow();
        assertTrue(updatedItem.isCompleted());
    }

    @Test
    @DisplayName("CompanyDossier enforces unique constraint per user and job")
    void companyDossierPersistenceAndUniqueness() {
        CompanyDossier dossier = CompanyDossier.create(
            candidateUser.getId(),
            testJob1.getId(),
            "Datadog",
            "GROWTH",
            "Observability platform",
            "10M events/sec",
            "Java, Spring, Kafka",
            "Blameless culture",
            "Stream processing",
            "Talking points",
            "Interviewer questions"
        );

        CompanyDossier saved = dossierRepository.save(dossier);
        assertNotNull(saved.getId());

        Optional<CompanyDossier> found = dossierRepository.findByUserIdAndJobId(candidateUser.getId(), testJob1.getId());
        assertTrue(found.isPresent());
        assertEquals("Datadog", found.get().getCompanyName());

        // Attempting to save a duplicate dossier for same user and job throws DataIntegrityViolationException
        CompanyDossier duplicate = CompanyDossier.create(
            candidateUser.getId(),
            testJob1.getId(),
            "Datadog Duplicate",
            "GROWTH",
            "Overview",
            "Scale",
            "Tech",
            "Culture",
            "Arch",
            "Points",
            "Questions"
        );

        assertThrows(DataIntegrityViolationException.class, () -> dossierRepository.save(duplicate));
    }
}
