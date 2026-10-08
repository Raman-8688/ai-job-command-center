package com.jobcommandcenter.ai;

import com.jobcommandcenter.ai.application.JobAiAnalysisService;
import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobAiAnalysisServiceUnitTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobAiAnalysisRepository analysisRepository;

    @Mock
    private AIProviderFactory providerFactory;

    @Mock
    private AIProvider aiProvider;

    private JobAiAnalysisService analysisService;
    private Job testJob;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        analysisService = new JobAiAnalysisService(
            jobRepository,
            analysisRepository,
            providerFactory
        );

        jobId = UUID.randomUUID();
        testJob = new Job(
            jobId,
            "EXT-101",
            "Senior Backend Engineer",
            "Cloud Services Inc",
            "https://cloud.io",
            "https://cloud.io/jobs/101",
            "Design and deploy high-volume Java and Spring Boot cloud services.",
            "San Francisco, CA",
            WorkMode.REMOTE,
            EmploymentType.FULL_TIME,
            new BigDecimal("5.0"),
            null,
            new BigDecimal("150000"),
            new BigDecimal("190000"),
            "USD",
            JobSource.COMPANY_CAREERS,
            "https://cloud.io/careers",
            Instant.now(),
            Instant.now(),
            null,
            JobStatus.ACTIVE,
            "hash123",
            Instant.now(),
            Instant.now(),
            List.of()
        );
    }

    @Test
    @DisplayName("analyzeJob increments version and persists completed analysis")
    void analyzeJobIncrementsVersionAndPersists() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(providerFactory.getActiveProvider()).thenReturn(aiProvider);
        when(aiProvider.getProviderName()).thenReturn("MOCK");
        when(aiProvider.getDefaultModel()).thenReturn("mock-model");

        when(analysisRepository.findMaxVersionByJobId(jobId)).thenReturn(1);

        AIJobAnalysisResponse mockResponse = new AIJobAnalysisResponse(
            "Senior Backend Engineer",
            "SENIOR",
            List.of("Design cloud services"),
            List.of(),
            List.of(new AnalyzedTechnology("Java", TechnologyCategory.LANGUAGE, true)),
            List.of("BS in Computer Science"),
            List.of(),
            "5+ years",
            List.of(),
            new BigDecimal("0.95"),
            "MOCK",
            "mock-model",
            "v1.0"
        );
        when(aiProvider.analyzeJob(any())).thenReturn(mockResponse);

        when(analysisRepository.save(any(JobAiAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        JobAiAnalysis result = analysisService.analyzeJob(jobId, null);

        assertThat(result).isNotNull();
        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(AIAnalysisStatus.COMPLETED);
        assertThat(result.getNormalizedTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(result.getTechnologies()).hasSize(1);
        assertThat(result.getCompletedAt()).isNotNull();

        verify(analysisRepository).save(any(JobAiAnalysis.class));
    }

    @Test
    @DisplayName("analyzeJob isolates provider failure and marks analysis as FAILED")
    void analyzeJobIsolatesProviderFailure() {
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(testJob));
        when(providerFactory.getActiveProvider()).thenReturn(aiProvider);
        when(aiProvider.getProviderName()).thenReturn("MOCK");
        when(aiProvider.getDefaultModel()).thenReturn("mock-model");
        when(analysisRepository.findMaxVersionByJobId(jobId)).thenReturn(0);

        when(aiProvider.analyzeJob(any())).thenThrow(new RuntimeException("External provider rate limit exceeded"));
        when(analysisRepository.save(any(JobAiAnalysis.class))).thenAnswer(inv -> inv.getArgument(0));

        JobAiAnalysis result = analysisService.analyzeJob(jobId, null);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AIAnalysisStatus.FAILED);
        assertThat(result.getErrorMessage()).contains("External provider rate limit exceeded");
        assertThat(result.getVersion()).isEqualTo(1);

        verify(analysisRepository).save(any(JobAiAnalysis.class));
    }

    @Test
    @DisplayName("getLatestAnalysis throws ResourceNotFoundException when no analysis exists")
    void getLatestAnalysisThrowsWhenNotFound() {
        when(jobRepository.existsById(jobId)).thenReturn(true);
        when(analysisRepository.findLatestByJobId(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analysisService.getLatestAnalysis(jobId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("No AI analysis found");
    }
}
