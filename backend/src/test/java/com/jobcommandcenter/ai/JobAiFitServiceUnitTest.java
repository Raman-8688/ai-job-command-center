package com.jobcommandcenter.ai;

import com.jobcommandcenter.ai.application.JobAiAnalysisService;
import com.jobcommandcenter.ai.application.JobAiFitService;
import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.job.application.JobMatchingService;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.skill.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobAiFitServiceUnitTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMatchingService jobMatchingService;

    @Mock
    private JobAiAnalysisService jobAiAnalysisService;

    @Mock
    private JobAiAnalysisRepository analysisRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    private JobAiFitService fitService;

    private UUID userId;
    private UUID jobId;
    private Job testJob;
    private JobAiAnalysis completedAnalysis;

    @BeforeEach
    void setUp() {
        fitService = new JobAiFitService(
            jobRepository,
            jobMatchingService,
            jobAiAnalysisService,
            analysisRepository,
            userSkillRepository,
            skillRepository
        );

        userId = UUID.randomUUID();
        jobId = UUID.randomUUID();

        testJob = new Job(
            jobId,
            "JOB-99",
            "Lead Java Architect",
            "Global Systems",
            "https://global.com",
            "https://global.com/jobs/99",
            "Looking for a Lead Java Architect skilled in Java, Spring Boot, and Kafka.",
            "Austin, TX",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("8.0"),
            null,
            null,
            null,
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://global.com/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash99",
            Instant.now(),
            Instant.now(),
            List.of()
        );

        completedAnalysis = new JobAiAnalysis(
            UUID.randomUUID(),
            jobId,
            1,
            "MOCK",
            "deterministic-rule-v1",
            "v1.0",
            AIAnalysisStatus.COMPLETED,
            "Lead Java Architect",
            "LEAD",
            List.of("Architect high-throughput streaming systems"),
            List.of(),
            List.of(
                new AnalyzedTechnology("Java", TechnologyCategory.LANGUAGE, true),
                new AnalyzedTechnology("Spring Boot", TechnologyCategory.FRAMEWORK, true),
                new AnalyzedTechnology("Kafka", TechnologyCategory.MESSAGING, true),
                new AnalyzedTechnology("Kubernetes", TechnologyCategory.DEVOPS, false)
            ),
            List.of("BS in Computer Science"),
            List.of(),
            "8+ years of experience",
            List.of(),
            new BigDecimal("0.95"),
            null,
            Instant.now(),
            Instant.now()
        );
    }

    @Test
    @DisplayName("evaluateFit ignores unverified skills and correctly categorizes missing required technologies")
    void evaluateFitIgnoresUnverifiedSkills() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(analysisRepository.findLatestByJobId(jobId)).thenReturn(Optional.of(completedAnalysis));

        JobMatchResult matchResult = new JobMatchResult(
            jobId,
            userId,
            85,
            90,
            80,
            70,
            90,
            80,
            80,
            List.of("Java", "Spring Boot"),
            List.of("Kafka"),
            List.of(),
            List.of("Kubernetes"),
            List.of("Strong skills match")
        );
        when(jobMatchingService.calculateMatch(userId, jobId)).thenReturn(matchResult);

        // Candidate has Java (verified), Spring Boot (verified), and Kafka (UNVERIFIED)
        Skill s1 = Skill.createNew("Java", SkillCategory.LANGUAGE);
        Skill s2 = Skill.createNew("Spring Boot", SkillCategory.FRAMEWORK);
        Skill s3 = Skill.createNew("Kafka", SkillCategory.TOOL);

        UserSkill verifiedJava = UserSkill.createNew(userId, s1.getId(), SkillProficiency.EXPERT, new BigDecimal("8.0"), true, VerificationSource.USER_EXPLICIT, "Verified");
        UserSkill verifiedSpring = UserSkill.createNew(userId, s2.getId(), SkillProficiency.EXPERT, new BigDecimal("6.0"), true, VerificationSource.USER_EXPLICIT, "Verified");
        UserSkill unverifiedKafka = UserSkill.createNew(userId, s3.getId(), SkillProficiency.BEGINNER, new BigDecimal("1.0"), false, VerificationSource.AI_SUGGESTED, "Unverified");

        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(verifiedJava, verifiedSpring, unverifiedKafka));
        when(skillRepository.findAll()).thenReturn(List.of(s1, s2, s3));

        JobAiFitEvaluation evaluation = fitService.evaluateFit(userId, jobId);

        assertThat(evaluation).isNotNull();
        // Deterministic score is preserved
        assertThat(evaluation.deterministicScore()).isEqualTo(85);
        // Verified skills matched
        assertThat(evaluation.matchedTechnologies()).containsExactlyInAnyOrder("Java", "Spring Boot");
        // Unverified Kafka MUST be listed in missing required technologies (anti-hallucination)
        assertThat(evaluation.missingRequiredTechnologies()).contains("Kafka");
        // Kubernetes was required = false, so it's in missing preferred
        assertThat(evaluation.missingPreferredTechnologies()).contains("Kubernetes");
        // Since 1 required tech is missing, tier is MODERATE_MATCH
        assertThat(evaluation.overallFitTier()).isEqualTo("MODERATE_MATCH");
    }

    @Test
    @DisplayName("evaluateFit evaluates STRONG_MATCH when all required technologies are verified")
    void evaluateFitEvaluatesStrongMatchWhenAllRequiredVerified() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(analysisRepository.findLatestByJobId(jobId)).thenReturn(Optional.of(completedAnalysis));

        JobMatchResult matchResult = new JobMatchResult(
            jobId,
            userId,
            92,
            90,
            95,
            80,
            90,
            90,
            90,
            List.of("Java", "Spring Boot", "Kafka"),
            List.of(),
            List.of(),
            List.of(),
            List.of("Exceptional match")
        );
        when(jobMatchingService.calculateMatch(userId, jobId)).thenReturn(matchResult);

        Skill s1 = Skill.createNew("Java", SkillCategory.LANGUAGE);
        Skill s2 = Skill.createNew("Spring Boot", SkillCategory.FRAMEWORK);
        Skill s3 = Skill.createNew("Kafka", SkillCategory.TOOL);

        UserSkill sJava = UserSkill.createNew(userId, s1.getId(), SkillProficiency.EXPERT, new BigDecimal("8.0"), true, VerificationSource.USER_EXPLICIT, "Verified");
        UserSkill sSpring = UserSkill.createNew(userId, s2.getId(), SkillProficiency.EXPERT, new BigDecimal("6.0"), true, VerificationSource.USER_EXPLICIT, "Verified");
        UserSkill sKafka = UserSkill.createNew(userId, s3.getId(), SkillProficiency.ADVANCED, new BigDecimal("4.0"), true, VerificationSource.USER_EXPLICIT, "Verified");

        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(sJava, sSpring, sKafka));
        when(skillRepository.findAll()).thenReturn(List.of(s1, s2, s3));

        JobAiFitEvaluation evaluation = fitService.evaluateFit(userId, jobId);

        assertThat(evaluation.overallFitTier()).isEqualTo("STRONG_MATCH");
        assertThat(evaluation.missingRequiredTechnologies()).isEmpty();
        assertThat(evaluation.matchedTechnologies()).contains("Java", "Spring Boot", "Kafka");
        assertThat(evaluation.interviewPreparationNotes()).isNotEmpty();
    }
}
