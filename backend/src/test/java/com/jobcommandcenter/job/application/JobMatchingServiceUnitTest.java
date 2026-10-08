package com.jobcommandcenter.job.application;

import com.jobcommandcenter.job.domain.*;
import com.jobcommandcenter.profile.domain.Profile;
import com.jobcommandcenter.profile.domain.WorkPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class JobMatchingServiceUnitTest {

    private JobMatchingService matchingService;

    private UUID javaSkillId;
    private UUID springSkillId;
    private UUID postgresSkillId;
    private UUID k8sSkillId;
    private UUID dockerSkillId;

    private Map<UUID, String> skillNames;

    @BeforeEach
    void setUp() {
        // Instantiate service without spring container for pure unit testing
        matchingService = new JobMatchingService(null, null, null, null);

        javaSkillId = UUID.randomUUID();
        springSkillId = UUID.randomUUID();
        postgresSkillId = UUID.randomUUID();
        k8sSkillId = UUID.randomUUID();
        dockerSkillId = UUID.randomUUID();

        skillNames = Map.of(
            javaSkillId, "Java",
            springSkillId, "Spring Boot",
            postgresSkillId, "PostgreSQL",
            k8sSkillId, "Kubernetes",
            dockerSkillId, "Docker"
        );
    }

    @Test
    @DisplayName("Explainability Rule: Missing REQUIRED skills must be prominently highlighted and never obscured by high score")
    void missingRequiredSkillsMustBeProminentlyFlagged() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Senior Java Developer", "Global Corp", null, null, "Great job",
            "Remote", WorkMode.REMOTE, EmploymentType.FULL_TIME,
            new BigDecimal("5.0"), new BigDecimal("10.0"), null, null, null, JobSource.MANUAL, null, null, null
        );

        // Required skills: Java, Spring Boot, Kubernetes
        // Preferred skill: Docker
        job.setJobSkills(List.of(
            JobSkill.create(job.getId(), javaSkillId, SkillRequirementType.REQUIRED, new BigDecimal("5.0")),
            JobSkill.create(job.getId(), springSkillId, SkillRequirementType.REQUIRED, new BigDecimal("4.0")),
            JobSkill.create(job.getId(), k8sSkillId, SkillRequirementType.REQUIRED, new BigDecimal("2.0")),
            JobSkill.create(job.getId(), dockerSkillId, SkillRequirementType.PREFERRED, BigDecimal.ONE)
        ));

        // Candidate has verified: Java, Spring Boot, Docker (Missing: Kubernetes)
        Set<UUID> candidateVerifiedSkills = Set.of(javaSkillId, springSkillId, dockerSkillId);

        Profile profile = new Profile(
            UUID.randomUUID(), userId, null, "Remote", null, null, null,
            List.of("Senior Java Developer"), List.of("Remote"), WorkPreference.REMOTE,
            new BigDecimal("7.0"), 0, null, null, Instant.now(), Instant.now()
        );

        JobMatchResult result = matchingService.computeMatch(job, profile, candidateVerifiedSkills, skillNames);

        assertThat(result.hasMissingRequiredSkills()).isTrue();
        assertThat(result.missingRequiredSkills()).containsExactly("Kubernetes");
        assertThat(result.matchedSkills()).containsExactlyInAnyOrder("Java", "Spring Boot");
        assertThat(result.matchedPreferredSkills()).containsExactly("Docker");

        // The very first reason must explicitly alert to the missing required skill
        assertThat(result.matchReasons().get(0)).contains("ATTENTION: Missing 1 required skill(s): [Kubernetes]");

        // Required skill score is 2 of 3 (67%)
        assertThat(result.requiredSkillScore()).isEqualTo(67);
    }

    @Test
    @DisplayName("Candidate with all required and preferred skills achieves top match")
    void allSkillsMatchedYieldsTopScore() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Senior Java Engineer", "Tech Stars", null, null, "Description",
            "San Francisco, CA", WorkMode.REMOTE, EmploymentType.FULL_TIME,
            new BigDecimal("3.0"), new BigDecimal("8.0"), null, null, null, JobSource.MANUAL, null, null, null
        );

        job.setJobSkills(List.of(
            JobSkill.create(job.getId(), javaSkillId, SkillRequirementType.REQUIRED, new BigDecimal("3.0")),
            JobSkill.create(job.getId(), postgresSkillId, SkillRequirementType.REQUIRED, new BigDecimal("2.0")),
            JobSkill.create(job.getId(), dockerSkillId, SkillRequirementType.PREFERRED, BigDecimal.ONE)
        ));

        Set<UUID> candidateVerifiedSkills = Set.of(javaSkillId, postgresSkillId, dockerSkillId);

        Profile profile = new Profile(
            UUID.randomUUID(), userId, null, "San Francisco, CA", null, null, null,
            List.of("Senior Java Engineer"), List.of("San Francisco, CA"), WorkPreference.REMOTE,
            new BigDecimal("5.0"), 0, null, null, Instant.now(), Instant.now()
        );

        JobMatchResult result = matchingService.computeMatch(job, profile, candidateVerifiedSkills, skillNames);

        assertThat(result.hasMissingRequiredSkills()).isFalse();
        assertThat(result.missingRequiredSkills()).isEmpty();
        assertThat(result.requiredSkillScore()).isEqualTo(100);
        assertThat(result.preferredSkillScore()).isEqualTo(100);
        assertThat(result.titleScore()).isEqualTo(100);
        assertThat(result.experienceScore()).isEqualTo(100);
        assertThat(result.workModeScore()).isEqualTo(100);
        assertThat(result.locationScore()).isEqualTo(100);
        assertThat(result.overallScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("Work mode mismatch penalizes score appropriately")
    void workModeMismatchPenalizesScore() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Backend Engineer", "Onsite Firm", null, null, "Description",
            "Chicago, IL", WorkMode.ONSITE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Profile profile = new Profile(
            UUID.randomUUID(), userId, null, "Remote", null, null, null,
            List.of("Backend Engineer"), List.of("Remote"), WorkPreference.REMOTE,
            new BigDecimal("4.0"), 0, null, null, Instant.now(), Instant.now()
        );

        JobMatchResult result = matchingService.computeMatch(job, profile, Set.of(), skillNames);

        assertThat(result.workModeScore()).isEqualTo(20);
        assertThat(result.matchReasons()).anyMatch(r -> r.contains("Candidate prefers REMOTE, but job is ONSITE"));
    }

    @Test
    @DisplayName("Candidate experience below minimum is penalized according to distance")
    void experienceUnderRequirementIsPenalized() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Lead Architect", "Enterprise Systems", null, null, "Description",
            null, WorkMode.REMOTE, EmploymentType.FULL_TIME,
            new BigDecimal("10.0"), null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Profile juniorProfile = new Profile(
            UUID.randomUUID(), userId, null, null, null, null, null,
            List.of("Lead Architect"), null, WorkPreference.REMOTE,
            new BigDecimal("3.0"), 0, null, null, Instant.now(), Instant.now()
        );

        JobMatchResult result = matchingService.computeMatch(job, juniorProfile, Set.of(), skillNames);

        assertThat(result.experienceScore()).isEqualTo(25);
        assertThat(result.matchReasons()).anyMatch(r -> r.contains("below minimum"));
    }

    @Test
    @DisplayName("Edge Case: Job with no skill requirements defaults to 100 on skill dimensions")
    void jobWithNoSkillsDefaultsToFullSkillScore() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "General Technologist", "Open Corp", null, null, "General tech",
            null, WorkMode.REMOTE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Profile profile = Profile.createNew(userId);
        JobMatchResult result = matchingService.computeMatch(job, profile, Set.of(), skillNames);

        assertThat(result.requiredSkillScore()).isEqualTo(100);
        assertThat(result.preferredSkillScore()).isEqualTo(100);
        assertThat(result.hasMissingRequiredSkills()).isFalse();
        assertThat(result.missingRequiredSkills()).isEmpty();
    }

    @Test
    @DisplayName("Edge Case: All required skills missing yields 0 on required skills score and flags all missing")
    void allRequiredSkillsMissingYieldsZeroScore() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Kubernetes Specialist", "Infra Inc", null, null, "K8s only",
            null, WorkMode.REMOTE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );
        job.setJobSkills(List.of(
            JobSkill.create(job.getId(), k8sSkillId, SkillRequirementType.REQUIRED, BigDecimal.ONE)
        ));

        // Candidate has Java, but not Kubernetes
        Profile profile = Profile.createNew(userId);
        JobMatchResult result = matchingService.computeMatch(job, profile, Set.of(javaSkillId), skillNames);

        assertThat(result.requiredSkillScore()).isEqualTo(0);
        assertThat(result.hasMissingRequiredSkills()).isTrue();
        assertThat(result.missingRequiredSkills()).containsExactly("Kubernetes");
    }

    @Test
    @DisplayName("Edge Case: Unspecified experience range on job yields 100 experience score")
    void missingExperienceRangeYieldsFullScore() {
        UUID userId = UUID.randomUUID();
        Job job = Job.createNew(
            null, "Junior/Senior Open Role", "Flex Corp", null, null, "Flexible experience",
            null, WorkMode.REMOTE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Profile profile = Profile.createNew(userId);
        JobMatchResult result = matchingService.computeMatch(job, profile, Set.of(), skillNames);

        assertThat(result.experienceScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("Edge Case: Location matching respects preferred locations and remote status")
    void locationMatchingRespectsPreferredLocations() {
        UUID userId = UUID.randomUUID();
        Job remoteJob = Job.createNew(
            null, "Remote Dev", "Remote Co", null, null, "Desc",
            "Anywhere", WorkMode.REMOTE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Job onsiteJob = Job.createNew(
            null, "Onsite Dev", "Austin Co", null, null, "Desc",
            "Austin, TX", WorkMode.ONSITE, EmploymentType.FULL_TIME,
            null, null, null, null, null, JobSource.MANUAL, null, null, null
        );

        Profile profile = new Profile(
            UUID.randomUUID(), userId, null, "Seattle, WA", null, null, null,
            List.of("Dev"), List.of("Austin, TX", "Seattle, WA"), WorkPreference.OPEN,
            BigDecimal.ZERO, 0, null, null, Instant.now(), Instant.now()
        );

        JobMatchResult remoteResult = matchingService.computeMatch(remoteJob, profile, Set.of(), skillNames);
        assertThat(remoteResult.locationScore()).isEqualTo(100);

        JobMatchResult onsiteResult = matchingService.computeMatch(onsiteJob, profile, Set.of(), skillNames);
        assertThat(onsiteResult.locationScore()).isEqualTo(100);
    }
}
