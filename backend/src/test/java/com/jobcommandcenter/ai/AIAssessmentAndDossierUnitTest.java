package com.jobcommandcenter.ai;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.MockDeterministicAIProvider;
import com.jobcommandcenter.ai.infrastructure.provider.OpenAIProvider;
import com.jobcommandcenter.assessment.domain.AssessmentChecklistItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AIAssessmentAndDossierUnitTest {

    private MockDeterministicAIProvider mockProvider;

    @BeforeEach
    void setUp() {
        mockProvider = new MockDeterministicAIProvider();
    }

    @Test
    @DisplayName("Assessment briefing produces deterministic output for identical inputs")
    void deterministicBriefingOutput() {
        AIAssessmentBriefingRequest request = new AIAssessmentBriefingRequest(
            "Senior Backend Engineer",
            "Acme Corp",
            "High throughput payment processing systems",
            "HACKERRANK",
            75,
            List.of("Distributed Systems", "Concurrency"),
            List.of("Java", "PostgreSQL"),
            "Focus on clean code"
        );

        AIAssessmentBriefingResponse res1 = mockProvider.generateAssessmentBriefing(request);
        AIAssessmentBriefingResponse res2 = mockProvider.generateAssessmentBriefing(request);

        assertThat(res1.platformGuidance()).isEqualTo(res2.platformGuidance());
        assertThat(res1.timeManagementAdvice()).isEqualTo(res2.timeManagementAdvice());
        assertThat(res1.prioritizedTopics()).isEqualTo(res2.prioritizedTopics());
        assertThat(res1.checklistItems()).hasSize(res2.checklistItems().size());
        for (int i = 0; i < res1.checklistItems().size(); i++) {
            assertThat(res1.checklistItems().get(i).title()).isEqualTo(res2.checklistItems().get(i).title());
            assertThat(res1.checklistItems().get(i).topicCategory()).isEqualTo(res2.checklistItems().get(i).topicCategory());
        }
        assertThat(res1.confidenceScore()).isEqualByComparingTo(res2.confidenceScore());
    }

    @Test
    @DisplayName("Platform-specific briefing tailors guidance across known assessment platforms")
    void platformSpecificBriefingGuidance() {
        String[] platforms = {"HACKERRANK", "LEETCODE", "CODESIGNAL", "KARAT", "CODERPAD", "TAKE_HOME", "TALENTLMS", "UNKNOWN_PLATFORM"};

        for (String platform : platforms) {
            AIAssessmentBriefingRequest req = new AIAssessmentBriefingRequest(
                "Software Engineer",
                "Target Tech",
                "Description",
                platform,
                60,
                List.of("Algorithms"),
                List.of("Java"),
                ""
            );
            AIAssessmentBriefingResponse resp = mockProvider.generateAssessmentBriefing(req);
            assertThat(resp.platformGuidance()).isNotBlank();

            switch (platform) {
                case "HACKERRANK" -> assertThat(resp.platformGuidance()).containsIgnoringCase("HackerRank").containsIgnoringCase("timeout");
                case "LEETCODE" -> assertThat(resp.platformGuidance()).containsIgnoringCase("LeetCode").containsIgnoringCase("Big-O");
                case "CODESIGNAL" -> assertThat(resp.platformGuidance()).containsIgnoringCase("CodeSignal").containsIgnoringCase("4-question");
                case "KARAT" -> assertThat(resp.platformGuidance()).containsIgnoringCase("Karat").containsIgnoringCase("debugging");
                case "CODERPAD" -> assertThat(resp.platformGuidance()).containsIgnoringCase("CoderPad").containsIgnoringCase("collaborative");
                case "TAKE_HOME" -> assertThat(resp.platformGuidance()).containsIgnoringCase("Take-Home").containsIgnoringCase("architecture");
                case "TALENTLMS" -> assertThat(resp.platformGuidance()).containsIgnoringCase("TalentLMS").containsIgnoringCase("Multiple-choice");
                default -> assertThat(resp.platformGuidance()).containsIgnoringCase("Standard technical assessment");
            }
        }
    }

    @Test
    @DisplayName("Pacing strategy scales appropriately according to allotted assessment duration")
    void briefingPacingCalculation() {
        AIAssessmentBriefingRequest req90m = new AIAssessmentBriefingRequest(
            "Engineer", "Co", "Desc", "HACKERRANK", 90, List.of(), List.of(), ""
        );
        AIAssessmentBriefingResponse resp90m = mockProvider.generateAssessmentBriefing(req90m);
        assertThat(resp90m.timeManagementAdvice()).contains("90-minute assessment");

        AIAssessmentBriefingRequest reqDefault = new AIAssessmentBriefingRequest(
            "Engineer", "Co", "Desc", "HACKERRANK", null, List.of(), List.of(), ""
        );
        AIAssessmentBriefingResponse respDefault = mockProvider.generateAssessmentBriefing(reqDefault);
        assertThat(respDefault.timeManagementAdvice()).contains("60-minute assessment");
    }

    @Test
    @DisplayName("Briefing response validator enforces schema, title length, non-empty list, and uniqueness")
    void briefingChecklistValidation() {
        // Valid briefing response passes validation
        AIAssessmentBriefingResponse valid = mockProvider.generateAssessmentBriefing(
            new AIAssessmentBriefingRequest("Engineer", "Co", "Desc", "HACKERRANK", 60, List.of(), List.of(), "")
        );
        AIAssessmentBriefingValidator.validateBriefingResponse(valid);

        // Blank platform guidance rejected
        AIAssessmentBriefingResponse blankGuidance = new AIAssessmentBriefingResponse(
            "  ", "Pacing", List.of(), valid.checklistItems(), new BigDecimal("0.9")
        );
        assertThatThrownBy(() -> AIAssessmentBriefingValidator.validateBriefingResponse(blankGuidance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Platform guidance must not be blank");

        // Empty checklist rejected
        AIAssessmentBriefingResponse emptyChecklist = new AIAssessmentBriefingResponse(
            "Guidance", "Pacing", List.of(), List.of(), new BigDecimal("0.9")
        );
        assertThatThrownBy(() -> AIAssessmentBriefingValidator.validateBriefingResponse(emptyChecklist))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("at least 1 item");

        // Duplicate title in checklist rejected
        AIAssessmentBriefingResponse dupes = new AIAssessmentBriefingResponse(
            "Guidance", "Pacing", List.of(),
            List.of(
                new AIAssessmentChecklistItem("ALGO", "Review Arrays", "Desc", 1),
                new AIAssessmentChecklistItem("ALGO", "Review Arrays", "Desc", 2)
            ),
            new BigDecimal("0.9")
        );
        assertThatThrownBy(() -> AIAssessmentBriefingValidator.validateBriefingResponse(dupes))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate checklist item title detected");
    }

    @Test
    @DisplayName("Checklist merging preserves candidate's existing items and completion state while appending novel items")
    void checklistMergingPreservesCandidateState() {
        UUID assessmentId = UUID.randomUUID();
        AssessmentChecklistItem completedItem = AssessmentChecklistItem.create(
            assessmentId, "ALGORITHMS", "Review Core Algorithmic Paradigms", "Existing desc", 1
        );
        completedItem.setCompleted(true);

        List<AssessmentChecklistItem> existing = List.of(completedItem);

        List<AIAssessmentChecklistItem> recommendations = List.of(
            new AIAssessmentChecklistItem("ALGORITHMS", "Review Core Algorithmic Paradigms", "New desc", 1),
            new AIAssessmentChecklistItem("EDGE_CASES", "Construct Boundary Test Cases", "New desc", 2)
        );

        List<AssessmentChecklistItem> merged = AIAssessmentBriefingValidator.mergeWithoutOverwriting(
            existing, recommendations, assessmentId
        );

        assertThat(merged).hasSize(2);
        // Original item kept intact
        assertThat(merged.get(0).getId()).isEqualTo(completedItem.getId());
        assertThat(merged.get(0).isCompleted()).isTrue();
        assertThat(merged.get(0).getTitle()).isEqualTo("Review Core Algorithmic Paradigms");

        // Novel item appended with sortOrder incremented
        assertThat(merged.get(1).getTitle()).isEqualTo("Construct Boundary Test Cases");
        assertThat(merged.get(1).isCompleted()).isFalse();
        assertThat(merged.get(1).getSortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("Company dossier produces deterministic output for identical inputs")
    void deterministicCompanyDossierOutput() {
        AICompanyDossierRequest request = new AICompanyDossierRequest(
            "Stripe",
            "Staff Infrastructure Engineer",
            "Scalable distributed ledgers and database clustering",
            List.of("Distributed Transactions", "Kubernetes", "Go"),
            List.of("Java", "PostgreSQL", "Kafka"),
            List.of("Led multi-region database migration"),
            "https://stripe.com",
            null
        );

        AICompanyDossierResponse res1 = mockProvider.generateCompanyDossier(request);
        AICompanyDossierResponse res2 = mockProvider.generateCompanyDossier(request);

        assertThat(res1.companyName()).isEqualTo(res2.companyName());
        assertThat(res1.companyTier()).isEqualTo(res2.companyTier());
        assertThat(res1.overview()).isEqualTo(res2.overview());
        assertThat(res1.coreTechStack()).isEqualTo(res2.coreTechStack());
        assertThat(res1.tailoredTalkingPoints()).isEqualTo(res2.tailoredTalkingPoints());
        assertThat(res1.interviewerQuestions()).isEqualTo(res2.interviewerQuestions());
        assertThat(res1.provenanceSummary()).isEqualTo(res2.provenanceSummary());
    }

    @Test
    @DisplayName("Company dossier grounds talking points strictly in candidate verified skills without hallucination")
    void dossierGroundedInVerifiedSkillsAndRequirements() {
        // Candidate with verified skills
        AICompanyDossierRequest withSkills = new AICompanyDossierRequest(
            "Acme Corp",
            "Platform Architect",
            "Cloud scaling",
            List.of("Spring Boot", "Kafka", "PostgreSQL"),
            List.of("Spring Boot", "Java"),
            List.of("Migrated monolith to microservices at Fintech Ltd"),
            "https://acme.org",
            null
        );

        AICompanyDossierResponse respWithSkills = mockProvider.generateCompanyDossier(withSkills);
        assertThat(respWithSkills.coreTechStack()).contains("Spring Boot", "Kafka", "PostgreSQL");
        assertThat(respWithSkills.tailoredTalkingPoints()).contains("Spring Boot", "Java");
        assertThat(respWithSkills.tailoredTalkingPoints()).contains("Migrated monolith to microservices");

        // Candidate with NO verified skills and NO experiences
        AICompanyDossierRequest withoutSkills = new AICompanyDossierRequest(
            "Acme Corp",
            "Platform Architect",
            "Cloud scaling",
            List.of("Spring Boot"),
            List.of(), // empty skills
            List.of(), // empty experiences
            "https://acme.org",
            null
        );

        AICompanyDossierResponse respNoSkills = mockProvider.generateCompanyDossier(withoutSkills);
        // Explicitly does NOT invent candidate skills
        assertThat(respNoSkills.tailoredTalkingPoints()).contains("No candidate verified skills or prior experience entries provided");
    }

    @Test
    @DisplayName("Company dossier distinguishes company research provenance when proprietary research is absent vs present")
    void dossierProvenanceDistinction() {
        // Absent research: provenance clarifies tier estimate
        AICompanyDossierRequest absentResearchReq = new AICompanyDossierRequest(
            "Fintech Dynamics", "Senior Engineer", "Job desc",
            List.of("Java"), List.of("Java"), List.of(), "https://fintech.io", null
        );
        AICompanyDossierResponse absentResp = mockProvider.generateCompanyDossier(absentResearchReq);
        assertThat(absentResp.provenanceSummary()).contains("general industry tier patterns as proprietary company research was not supplied");
        assertThat(absentResp.overview()).contains("Company-specific internal telemetry was not supplied");

        // Supplied research: incorporates research
        AICompanyDossierRequest suppliedResearchReq = new AICompanyDossierRequest(
            "Fintech Dynamics", "Senior Engineer", "Job desc",
            List.of("Java"), List.of("Java"), List.of(), "https://fintech.io",
            "Fintech Dynamics recently expanded to APAC with an in-house real-time settlement engine."
        );
        AICompanyDossierResponse suppliedResp = mockProvider.generateCompanyDossier(suppliedResearchReq);
        assertThat(suppliedResp.overview()).contains("in-house real-time settlement engine");
        assertThat(suppliedResp.provenanceSummary()).contains("Company context supplemented by verified source research");
    }

    @Test
    @DisplayName("Company tier is deterministically resolved based on company profile cues")
    void dossierCompanyTierClassification() {
        assertThat(mockProvider.generateCompanyDossier(new AICompanyDossierRequest("Google", "Role", "Desc", List.of(), List.of(), List.of(), "", null)).companyTier()).isEqualTo("TIER_1_TECH");
        assertThat(mockProvider.generateCompanyDossier(new AICompanyDossierRequest("Amazon Web Services", "Role", "Desc", List.of(), List.of(), List.of(), "", null)).companyTier()).isEqualTo("TIER_1_TECH");
        assertThat(mockProvider.generateCompanyDossier(new AICompanyDossierRequest("Modern AI Labs", "Role", "Desc", List.of(), List.of(), List.of(), "", null)).companyTier()).isEqualTo("GROWTH_STARTUP");
        assertThat(mockProvider.generateCompanyDossier(new AICompanyDossierRequest("Chase Bank Capital", "Role", "Desc", List.of(), List.of(), List.of(), "", null)).companyTier()).isEqualTo("FINANCIAL_TECH");
        assertThat(mockProvider.generateCompanyDossier(new AICompanyDossierRequest("General Logistics Corp", "Role", "Desc", List.of(), List.of(), List.of(), "", null)).companyTier()).isEqualTo("ENTERPRISE");
    }

    @Test
    @DisplayName("OpenAIProvider enforces availability guards and safe error handling without leaking secrets")
    void openAiProviderGuardsAndSafety() {
        OpenAIProvider unconfigured = new OpenAIProvider("gpt-4o-mini", "");
        assertThat(unconfigured.isAvailable()).isFalse();

        AIAssessmentBriefingRequest briefingReq = new AIAssessmentBriefingRequest(
            "Role", "Company", "Desc", "HACKERRANK", 60, List.of(), List.of(), ""
        );
        assertThatThrownBy(() -> unconfigured.generateAssessmentBriefing(briefingReq))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("OpenAI API key is not configured");

        AICompanyDossierRequest dossierReq = new AICompanyDossierRequest(
            "Company", "Role", "Desc", List.of(), List.of(), List.of(), "", null
        );
        assertThatThrownBy(() -> unconfigured.generateCompanyDossier(dossierReq))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("OpenAI API key is not configured");

        OpenAIProvider configured = new OpenAIProvider("gpt-4o-mini", "sk-test-mock-key-12345");
        assertThat(configured.isAvailable()).isTrue();
        assertThatThrownBy(() -> configured.generateAssessmentBriefing(briefingReq))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("OpenAI remote calls disabled in test profile");

        assertThatThrownBy(() -> configured.generateCompanyDossier(dossierReq))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("OpenAI remote calls disabled in test profile");
    }
}
