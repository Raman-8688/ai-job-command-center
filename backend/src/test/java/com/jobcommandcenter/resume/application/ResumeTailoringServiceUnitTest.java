package com.jobcommandcenter.resume.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.ai.infrastructure.provider.MockDeterministicAIProvider;
import com.jobcommandcenter.job.domain.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeTailoringServiceUnitTest {

    @Mock
    private ResumeService resumeService;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAiAnalysisRepository jobAiAnalysisRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private TailoredResumeRepository tailoredResumeRepository;

    @Mock
    private AIProviderFactory aiProviderFactory;

    private ResumeTailoringService service;
    private final MockDeterministicAIProvider mockAIProvider = new MockDeterministicAIProvider();

    @BeforeEach
    void setUp() {
        lenient().when(aiProviderFactory.getActiveProvider()).thenReturn(mockAIProvider);
        service = new ResumeTailoringService(
            resumeService,
            jobRepository,
            jobAiAnalysisRepository,
            userSkillRepository,
            skillRepository,
            tailoredResumeRepository,
            aiProviderFactory
        );
    }

    @Test
    @DisplayName("createTailoredDraft creates v1 and correctly tags unverified skills as NOT_ENOUGH_EVIDENCE")
    void createTailoredDraftVersionAndAntiHallucination() {
        UUID userId = UUID.randomUUID();
        UUID resumeId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Resume masterResume = Resume.create(
            userId,
            "Master Resume",
            "Software Engineer",
            "General background",
            new BigDecimal("5.0"),
            "Remote",
            "test@example.com",
            "123456"
        );

        UUID javaSkillId = UUID.randomUUID();
        UUID kafkaSkillId = UUID.randomUUID();

        Skill javaSkill = Skill.createNew("Java", SkillCategory.LANGUAGE);
        Skill kafkaSkill = Skill.createNew("Kafka", SkillCategory.FRAMEWORK);

        Job job = new Job(
            jobId,
            "JOB-101",
            "Senior Backend Engineer",
            "Acme Corp",
            "https://acme.com",
            "https://acme.com/jobs/101",
            "Java and Kafka required.",
            "Remote",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("4.0"),
            null,
            null,
            null,
            "USD",
            JobSource.MANUAL,
            null,
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash101",
            Instant.now(),
            Instant.now(),
            List.of(
                JobSkill.create(jobId, javaSkillId, SkillRequirementType.REQUIRED, BigDecimal.valueOf(3)),
                JobSkill.create(jobId, kafkaSkillId, SkillRequirementType.REQUIRED, BigDecimal.valueOf(2))
            )
        );

        // Candidate ONLY has verified Java; Kafka is NOT verified
        UserSkill userJavaSkill = UserSkill.createNew(
            userId,
            javaSkillId,
            SkillProficiency.EXPERT,
            BigDecimal.valueOf(4),
            true,
            VerificationSource.USER_EXPLICIT,
            "Self verified"
        );

        when(resumeService.getOwnedResume(userId, resumeId)).thenReturn(masterResume);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(tailoredResumeRepository.findMaxVersion(resumeId, jobId)).thenReturn(0);
        when(skillRepository.findAll()).thenReturn(List.of(
            new Skill(javaSkillId, "Java", null, SkillCategory.LANGUAGE, Instant.now(), Instant.now()),
            new Skill(kafkaSkillId, "Kafka", null, SkillCategory.FRAMEWORK, Instant.now(), Instant.now())
        ));
        when(jobAiAnalysisRepository.findLatestByJobId(jobId)).thenReturn(Optional.empty());
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(userJavaSkill));
        when(tailoredResumeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TailoredResume draft = service.createTailoredDraft(userId, resumeId, jobId);

        assertThat(draft).isNotNull();
        assertThat(draft.getVersion()).isEqualTo(1);
        assertThat(draft.getStatus()).isEqualTo(TailoredResumeStatus.DRAFT);
        assertThat(draft.getMatchedKeywords()).contains("Java");
        assertThat(draft.getMissingKeywords()).contains("Kafka");

        // Verify anti-hallucination check in suggestions:
        Optional<TailoredResumeSuggestion> kafkaSuggestion = draft.getSuggestions().stream()
            .filter(s -> s.getTargetItemTitle().contains("Kafka"))
            .findFirst();

        assertThat(kafkaSuggestion).isPresent();
        assertThat(kafkaSuggestion.get().getVerificationStatus()).isEqualTo("NOT_ENOUGH_EVIDENCE");
        assertThat(kafkaSuggestion.get().getSuggestedContent()).contains("[NOT_ENOUGH_EVIDENCE]");
    }

    @Test
    @DisplayName("createTailoredDraft increments version when previous versions exist")
    void createTailoredDraftIncrementsVersion() {
        UUID userId = UUID.randomUUID();
        UUID resumeId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        Resume masterResume = Resume.create(userId, "Master", "Dev", "Summary", BigDecimal.ONE, "NYC", null, null);
        Job job = new Job(
            jobId,
            "JOB-102",
            "Backend Engineer",
            "Beta LLC",
            null,
            null,
            "Description",
            "Remote",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            null,
            null,
            null,
            null,
            "USD",
            JobSource.MANUAL,
            null,
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash102",
            Instant.now(),
            Instant.now(),
            List.of()
        );

        when(resumeService.getOwnedResume(userId, resumeId)).thenReturn(masterResume);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(tailoredResumeRepository.findMaxVersion(resumeId, jobId)).thenReturn(2); // Existing v1, v2
        when(skillRepository.findAll()).thenReturn(List.of());
        when(jobAiAnalysisRepository.findLatestByJobId(jobId)).thenReturn(Optional.empty());
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of());
        when(tailoredResumeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TailoredResume draft = service.createTailoredDraft(userId, resumeId, jobId);

        assertThat(draft.getVersion()).isEqualTo(3);
    }
}
