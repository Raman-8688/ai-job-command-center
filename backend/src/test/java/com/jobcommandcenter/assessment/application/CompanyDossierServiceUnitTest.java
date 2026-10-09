package com.jobcommandcenter.assessment.application;

import com.jobcommandcenter.ai.domain.AICompanyDossierResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.assessment.api.dto.CompanyDossierResponse;
import com.jobcommandcenter.assessment.api.dto.GenerateCompanyDossierRequest;
import com.jobcommandcenter.assessment.domain.CompanyDossier;
import com.jobcommandcenter.assessment.domain.CompanyDossierRepository;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.skill.domain.Skill;
import com.jobcommandcenter.skill.domain.SkillCategory;
import com.jobcommandcenter.skill.domain.SkillRepository;
import com.jobcommandcenter.skill.domain.UserSkill;
import com.jobcommandcenter.skill.domain.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyDossierServiceUnitTest {

    @Mock
    private CompanyDossierRepository dossierRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AIProvider aiProvider;

    private CompanyDossierService dossierService;

    private final UUID userId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private Job testJob;

    @BeforeEach
    void setUp() {
        dossierService = new CompanyDossierService(
            dossierRepository,
            jobRepository,
            userSkillRepository,
            skillRepository,
            aiProvider
        );

        testJob = Job.createNew(
            "ext-stripe-1",
            "Staff Infrastructure Engineer",
            "Stripe",
            "https://stripe.com",
            "https://stripe.com/jobs/1",
            "Distributed ledger and transaction infrastructure",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("6.0"),
            new BigDecimal("12.0"),
            new BigDecimal("210000"),
            new BigDecimal("280000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://stripe.com",
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(30, ChronoUnit.DAYS)
        );
    }

    @Test
    @DisplayName("Get dossier for job returns dossier when present")
    void getDossierSuccess() {
        CompanyDossier dossier = CompanyDossier.create(
            userId, jobId, "Stripe", "TIER_1_TECH", "Overview", "Scale", "Java, Go", "Culture", "Architecture", "Talking Points", "Questions"
        );

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(dossierRepository.findByUserIdAndJobId(userId, jobId)).thenReturn(Optional.of(dossier));

        CompanyDossierResponse response = dossierService.getDossierForJob(userId, jobId);

        assertThat(response).isNotNull();
        assertThat(response.companyName()).isEqualTo("Stripe");
        assertThat(response.companyTier()).isEqualTo("TIER_1_TECH");
        assertThat(response.coreTechStack()).isEqualTo("Java, Go");
    }

    @Test
    @DisplayName("Get dossier throws ResourceNotFoundException when dossier not generated yet")
    void getDossierThrowsNotFound() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(dossierRepository.findByUserIdAndJobId(userId, jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dossierService.getDossierForJob(userId, jobId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Company dossier not found for job");
    }

    @Test
    @DisplayName("Generate dossier creates new dossier on first invocation")
    void generateDossierCreatesNew() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of());
        when(dossierRepository.findByUserIdAndJobId(userId, jobId)).thenReturn(Optional.empty());
        when(dossierRepository.save(any(CompanyDossier.class))).thenAnswer(inv -> inv.getArgument(0));

        AICompanyDossierResponse aiResp = new AICompanyDossierResponse(
            "Stripe", "TIER_1_TECH", "Overview", "Scale", "Java, Go, Kafka", "Culture", "Architecture", "Talking Points", "Questions", "Provenance", new BigDecimal("0.92")
        );
        when(aiProvider.generateCompanyDossier(any())).thenReturn(aiResp);

        CompanyDossierResponse response = dossierService.generateOrRefreshDossier(userId, jobId, new GenerateCompanyDossierRequest(null));

        assertThat(response).isNotNull();
        assertThat(response.companyName()).isEqualTo("Stripe");
        assertThat(response.companyTier()).isEqualTo("TIER_1_TECH");
        assertThat(response.coreTechStack()).isEqualTo("Java, Go, Kafka");
        assertThat(response.provenanceSummary()).isEqualTo("Provenance");
    }

    @Test
    @DisplayName("Generate dossier refreshes existing dossier in-place preserving ID and timestamps")
    void refreshDossierUpdatesInPlace() {
        CompanyDossier existing = CompanyDossier.create(
            userId, jobId, "Stripe", "ENTERPRISE", "Old overview", "Old scale", "Old stack", "Old culture", "Old arch", "Old points", "Old questions"
        );
        UUID originalId = existing.getId();
        Instant originalCreatedAt = existing.getCreatedAt();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of());
        when(dossierRepository.findByUserIdAndJobId(userId, jobId)).thenReturn(Optional.of(existing));
        when(dossierRepository.save(any(CompanyDossier.class))).thenAnswer(inv -> inv.getArgument(0));

        AICompanyDossierResponse aiResp = new AICompanyDossierResponse(
            "Stripe", "TIER_1_TECH", "New overview", "New scale", "New stack", "New culture", "New arch", "New points", "New questions", "Updated provenance", new BigDecimal("0.95")
        );
        when(aiProvider.generateCompanyDossier(any())).thenReturn(aiResp);

        CompanyDossierResponse response = dossierService.generateOrRefreshDossier(userId, jobId, new GenerateCompanyDossierRequest("Research notes"));

        assertThat(response.id()).isEqualTo(originalId);
        assertThat(response.createdAt()).isEqualTo(originalCreatedAt);
        assertThat(response.companyTier()).isEqualTo("TIER_1_TECH");
        assertThat(response.overview()).isEqualTo("New overview");
        assertThat(response.coreTechStack()).isEqualTo("New stack");
    }
}
