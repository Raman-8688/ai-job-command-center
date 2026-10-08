package com.jobcommandcenter.resume;

import com.jobcommandcenter.ai.application.JobAiAnalysisService;
import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.resume.application.JobResumeAnalysisService;
import com.jobcommandcenter.resume.application.ResumeService;
import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobResumeAnalysisServiceUnitTest {

    @Mock
    private ResumeService resumeService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAiAnalysisRepository jobAiAnalysisRepository;

    @Mock
    private JobAiAnalysisService jobAiAnalysisService;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AIProviderFactory aiProviderFactory;

    @Mock
    private AIProvider aiProvider;

    private JobResumeAnalysisService analysisService;

    private UUID userId;
    private UUID resumeId;
    private UUID jobId;
    private Resume testResume;
    private Job testJob;

    private Skill javaSkill;
    private Skill springSkill;
    private Skill postgresSkill;
    private Skill redisSkill;
    private Skill kafkaSkill;

    @BeforeEach
    void setUp() {
        analysisService = new JobResumeAnalysisService(
            resumeService,
            jobRepository,
            jobAiAnalysisRepository,
            jobAiAnalysisService,
            userSkillRepository,
            skillRepository,
            aiProviderFactory
        );

        userId = UUID.randomUUID();
        resumeId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        // Skills catalog
        javaSkill = new Skill(UUID.randomUUID(), "Java", "java", SkillCategory.LANGUAGE, Instant.now(), Instant.now());
        springSkill = new Skill(UUID.randomUUID(), "Spring Boot", "spring boot", SkillCategory.FRAMEWORK, Instant.now(), Instant.now());
        postgresSkill = new Skill(UUID.randomUUID(), "PostgreSQL", "postgresql", SkillCategory.DATABASE, Instant.now(), Instant.now());
        redisSkill = new Skill(UUID.randomUUID(), "Redis", "redis", SkillCategory.DATABASE, Instant.now(), Instant.now());
        kafkaSkill = new Skill(UUID.randomUUID(), "Kafka", "kafka", SkillCategory.TOOL, Instant.now(), Instant.now());

        // Resume: Contains Java and Spring Boot, but does NOT mention Redis or Kafka
        testResume = new Resume(
            resumeId,
            userId,
            "Backend Java Resume",
            "Senior Backend Engineer",
            "Specialized in high-scale Java platforms.",
            new BigDecimal("6.0"),
            "Austin, TX",
            "alex@example.com",
            null,
            ResumeStatus.ACTIVE,
            Instant.now(),
            Instant.now(),
            List.of(
                new ResumeExperience(
                    UUID.randomUUID(),
                    "Apex Global",
                    "Senior Software Engineer",
                    LocalDate.of(2021, 6, 1),
                    null,
                    true,
                    "Remote",
                    "Architected high-scale microservices using Java and Spring Boot.",
                    List.of("Increased system reliability to 99.99%"),
                    List.of("Java", "Spring Boot"),
                    0
                )
            ),
            List.of(
                new ResumeProject(
                    UUID.randomUUID(),
                    "Order Management Service",
                    "Distributed order pipeline built with Java and PostgreSQL",
                    "Backend Lead",
                    List.of("Java", "PostgreSQL"),
                    List.of("Implemented idempotency keys"),
                    List.of("Handled 500k RPS peak"),
                    "1 year",
                    "https://github.com/alex/orders",
                    0
                )
            ),
            List.of(
                new ResumeSkill(UUID.randomUUID(), javaSkill.getId(), "Java", SkillProficiency.EXPERT, new BigDecimal("6.0")),
                new ResumeSkill(UUID.randomUUID(), springSkill.getId(), "Spring Boot", SkillProficiency.ADVANCED, new BigDecimal("4.0"))
            ),
            List.of(),
            List.of()
        );

        // Job: Requires Java, Spring Boot, and Kafka (preferred: Docker)
        testJob = new Job(
            jobId,
            "JOB-500",
            "Staff Backend Engineer",
            "Fintech Corp",
            "https://fintech.io",
            "https://fintech.io/jobs/500",
            "Looking for Staff Backend Engineer with Java, Spring Boot, and Kafka experience.",
            "Austin, TX",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            new BigDecimal("8.0"),
            null,
            null,
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://fintech.io/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash500",
            Instant.now(),
            Instant.now(),
            List.of(
                JobSkill.create(jobId, javaSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("5.0")),
                JobSkill.create(jobId, springSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("3.0")),
                JobSkill.create(jobId, kafkaSkill.getId(), SkillRequirementType.REQUIRED, new BigDecimal("2.0"))
            )
        );
    }

    @Test
    @DisplayName("analyzeResumeForJob identifies strong matches, missing required skills, and verified skills omitted from resume")
    void analyzeResumeForJobComplete() {
        when(resumeService.getOwnedResume(userId, resumeId)).thenReturn(testResume);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));

        // Catalog mock
        when(skillRepository.findAll()).thenReturn(List.of(javaSkill, springSkill, postgresSkill, redisSkill, kafkaSkill));

        // Verified skills: Candidate has verified Java, Spring Boot, and REDIS!
        UserSkill vJava = UserSkill.createNew(userId, javaSkill.getId(), SkillProficiency.EXPERT, new BigDecimal("6.0"), true, VerificationSource.USER_EXPLICIT, "V");
        UserSkill vSpring = UserSkill.createNew(userId, springSkill.getId(), SkillProficiency.ADVANCED, new BigDecimal("4.0"), true, VerificationSource.USER_EXPLICIT, "V");
        UserSkill vRedis = UserSkill.createNew(userId, redisSkill.getId(), SkillProficiency.ADVANCED, new BigDecimal("3.0"), true, VerificationSource.USER_EXPLICIT, "V");
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(vJava, vSpring, vRedis));

        // AI Provider mock
        when(aiProviderFactory.getActiveProvider()).thenReturn(aiProvider);
        when(aiProvider.analyzeResumeFit(any())).thenReturn(new AIResumeAnalysisResponse(
            "Strong candidate background for Fintech Corp.",
            List.of("Experience directly aligns with distributed architecture."),
            List.of("Required technology 'Kafka' is not represented on the resume. [NOT_ENOUGH_EVIDENCE] Candidate has no verified proof in skill profile; do not fabricate experience."),
            new BigDecimal("0.94")
        ));

        JobResumeAnalysisResult result = analysisService.analyzeResumeForJob(userId, resumeId, jobId);

        assertThat(result).isNotNull();
        assertThat(result.resumeId()).isEqualTo(resumeId);
        assertThat(result.jobId()).isEqualTo(jobId);

        // 1. Strong matches: Java & Spring Boot exist on resume and match job
        assertThat(result.strongMatches()).contains("Java", "Spring Boot");

        // 2. Missing required skill: Kafka is required by job but absent from resume
        assertThat(result.missingRequiredSkills()).contains("Kafka");

        // 3. Verified skill missing from resume: Candidate has verified Redis, but resume lacks it!
        assertThat(result.verifiedSkillsMissingFromResume()).contains("Redis");

        // 4. Resume evidence: Java found in Experience and Skill
        assertThat(result.resumeEvidence()).isNotEmpty();
        assertThat(result.resumeEvidence()).anyMatch(e -> e.skillOrRequirement().equals("Java") && e.section().equals("EXPERIENCE"));
        assertThat(result.resumeEvidence()).anyMatch(e -> e.skillOrRequirement().equals("Spring Boot") && e.section().equals("SKILL"));

        // 5. Experience alignment
        assertThat(result.experienceAlignment().candidateYearsExperience()).isEqualByComparingTo("6.0");
        assertThat(result.experienceAlignment().meetsExperienceRequirement()).isTrue();

        // 6. Anti-hallucination check in suggestions
        assertThat(result.improvementSuggestions()).anyMatch(s -> s.contains("[NOT_ENOUGH_EVIDENCE]"));
    }
}
