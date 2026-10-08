package com.jobcommandcenter.job.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobUnitTest {

    @Test
    @DisplayName("Deduplication hash must be deterministic, lowercase, and whitespace-trimmed")
    void deduplicationHashIsDeterministic() {
        String hash1 = Job.calculateDeduplicationHash("Acme Corp", "Senior Java Engineer", "New York, NY");
        String hash2 = Job.calculateDeduplicationHash("  acme corp  ", "senior java engineer", "  new york, ny ");
        String hash3 = Job.calculateDeduplicationHash("Other Corp", "Senior Java Engineer", "New York, NY");

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256 hex string
        assertThat(hash1).isNotEqualTo(hash3);
    }

    @Test
    @DisplayName("Job creation validates required fields and preserves raw description")
    void jobCreationValidatesMandatoryFields() {
        String rawDesc = "Detailed original job description with formatting.\nNo alteration permitted.";
        Job job = Job.createNew(
            "EXT-101",
            "Staff Backend Engineer",
            "Tech Innovators",
            "https://techinnovators.io",
            "https://techinnovators.io/careers/101",
            rawDesc,
            "Remote, US",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("6.0"),
            new BigDecimal("10.0"),
            new BigDecimal("150000"),
            new BigDecimal("190000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://techinnovators.io/careers",
            Instant.now(),
            Instant.now().plusSeconds(86400 * 30)
        );

        assertThat(job.getId()).isNotNull();
        assertThat(job.getTitle()).isEqualTo("Staff Backend Engineer");
        assertThat(job.getCompanyName()).isEqualTo("Tech Innovators");
        assertThat(job.getDescription()).isEqualTo(rawDesc);
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.getWorkMode()).isEqualTo(WorkMode.REMOTE);
        assertThat(job.getDeduplicationHash()).isNotBlank();
    }

    @Test
    @DisplayName("Job creation rejects blank title, company or description")
    void jobCreationRejectsBlankFields() {
        assertThatThrownBy(() -> Job.createNew(
            null, "  ", "Company", null, null, "Desc", "Loc", null, null, null, null, null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("title cannot be blank");

        assertThatThrownBy(() -> Job.createNew(
            null, "Title", "  ", null, null, "Desc", "Loc", null, null, null, null, null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Company name cannot be blank");

        assertThatThrownBy(() -> Job.createNew(
            null, "Title", "Company", null, null, "  ", "Loc", null, null, null, null, null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("description cannot be blank");
    }

    @Test
    @DisplayName("JobSkills can be updated on a job")
    void jobSkillsCanBeUpdated() {
        Job job = Job.createNew(
            null, "Backend Developer", "Startup Inc", null, null, "Description", "Bengaluru",
            WorkMode.HYBRID, EmploymentType.FULL_TIME, null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        UUID skillId1 = UUID.randomUUID();
        UUID skillId2 = UUID.randomUUID();

        JobSkill reqSkill = JobSkill.create(job.getId(), skillId1, SkillRequirementType.REQUIRED, new BigDecimal("3.0"));
        JobSkill prefSkill = JobSkill.create(job.getId(), skillId2, SkillRequirementType.PREFERRED, BigDecimal.ONE);

        job.setJobSkills(List.of(reqSkill, prefSkill));

        assertThat(job.getJobSkills()).hasSize(2);
        assertThat(job.getJobSkills().get(0).isRequired()).isTrue();
        assertThat(job.getJobSkills().get(1).isPreferred()).isTrue();
    }
}
