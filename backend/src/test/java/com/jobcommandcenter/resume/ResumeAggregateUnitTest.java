package com.jobcommandcenter.resume;

import com.jobcommandcenter.resume.domain.*;
import com.jobcommandcenter.skill.domain.SkillProficiency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeAggregateUnitTest {

    @Test
    @DisplayName("Resume aggregate creates with DRAFT status and validates non-blank name")
    void resumeCreationAndValidation() {
        UUID userId = UUID.randomUUID();
        Resume resume = Resume.create(
            userId,
            "Backend Java Resume",
            "Senior Java Developer",
            "Passionate engineer with 6 years experience.",
            new BigDecimal("6.0"),
            "Seattle, WA",
            "candidate@example.com",
            "+1-555-0199"
        );

        assertThat(resume.getId()).isNotNull();
        assertThat(resume.getUserId()).isEqualTo(userId);
        assertThat(resume.getName()).isEqualTo("Backend Java Resume");
        assertThat(resume.getStatus()).isEqualTo(ResumeStatus.DRAFT);
        assertThat(resume.getYearsOfExperience()).isEqualByComparingTo("6.0");
        assertThat(resume.isOwnedBy(userId)).isTrue();
        assertThat(resume.isOwnedBy(UUID.randomUUID())).isFalse();

        assertThatThrownBy(() -> Resume.create(userId, "  ", null, null, null, null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Resume name cannot be blank");
    }

    @Test
    @DisplayName("Resume lifecycle transitions from DRAFT to ACTIVE to ARCHIVED")
    void resumeLifecycleTransitions() {
        Resume resume = Resume.create(
            UUID.randomUUID(),
            "Full Stack Resume",
            "Full Stack Engineer",
            "Overview",
            new BigDecimal("4.0"),
            "Remote",
            null,
            null
        );

        assertThat(resume.getStatus()).isEqualTo(ResumeStatus.DRAFT);

        resume.activate();
        assertThat(resume.getStatus()).isEqualTo(ResumeStatus.ACTIVE);

        resume.archive();
        assertThat(resume.getStatus()).isEqualTo(ResumeStatus.ARCHIVED);
    }

    @Test
    @DisplayName("Resume aggregate mutates structured sections correctly")
    void resumeSectionMutations() {
        Resume resume = Resume.create(
            UUID.randomUUID(),
            "Cloud Platform Resume",
            "Cloud Architect",
            "Cloud summary",
            new BigDecimal("8.0"),
            "San Francisco, CA",
            null,
            null
        );

        // Experience
        ResumeExperience exp = ResumeExperience.create(
            "Fintech Corp",
            "Senior Backend Engineer",
            LocalDate.of(2021, 1, 1),
            null,
            true,
            "San Francisco, CA",
            "Architected microservices",
            List.of("Cut API latency by 40%"),
            List.of("Java", "Spring Boot", "Kafka"),
            0
        );
        resume.setExperiences(List.of(exp));
        assertThat(resume.getExperiences()).hasSize(1);
        assertThat(resume.getExperiences().get(0).getCompany()).isEqualTo("Fintech Corp");
        assertThat(resume.getExperiences().get(0).isCurrentlyWorking()).isTrue();

        // Project
        ResumeProject proj = ResumeProject.create(
            "Payment Gateway",
            "High throughput payment router",
            "Tech Lead",
            List.of("Java", "PostgreSQL", "Docker"),
            List.of("Designed transaction isolation"),
            List.of("Processed 1M tx/day"),
            "6 months",
            "https://github.com/candidate/payments",
            0
        );
        resume.setProjects(List.of(proj));
        assertThat(resume.getProjects()).hasSize(1);

        // Skill
        UUID skillId = UUID.randomUUID();
        ResumeSkill skill = ResumeSkill.create(skillId, "Java", SkillProficiency.EXPERT, new BigDecimal("8.0"));
        resume.setSkills(List.of(skill));
        assertThat(resume.getSkills()).hasSize(1);
        assertThat(resume.getSkills().get(0).getSkillName()).isEqualTo("Java");

        // Education
        ResumeEducation edu = ResumeEducation.create(
            "Stanford University",
            "B.S.",
            "Computer Science",
            2014,
            2018,
            0
        );
        resume.setEducation(List.of(edu));
        assertThat(resume.getEducation()).hasSize(1);

        // Certification
        ResumeCertification cert = ResumeCertification.create(
            "AWS Certified Solutions Architect",
            "Amazon Web Services",
            LocalDate.of(2023, 5, 1),
            LocalDate.of(2026, 5, 1),
            "AWS-12345",
            null,
            0
        );
        resume.setCertifications(List.of(cert));
        assertThat(resume.getCertifications()).hasSize(1);
    }
}
