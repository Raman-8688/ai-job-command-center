package com.jobcommandcenter.ai.application;

import com.jobcommandcenter.ai.domain.*;
import com.jobcommandcenter.ai.infrastructure.provider.AIProviderFactory;
import com.jobcommandcenter.common.error.ResourceNotFoundException;
import com.jobcommandcenter.job.domain.Job;
import com.jobcommandcenter.job.domain.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service orchestrating AI job analysis workflows,
 * provider delegation, versioning, and persistence.
 */
@Service
@Transactional
public class JobAiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(JobAiAnalysisService.class);

    private final JobRepository jobRepository;
    private final JobAiAnalysisRepository analysisRepository;
    private final AIProviderFactory providerFactory;

    public JobAiAnalysisService(JobRepository jobRepository,
                                JobAiAnalysisRepository analysisRepository,
                                AIProviderFactory providerFactory) {
        this.jobRepository = jobRepository;
        this.analysisRepository = analysisRepository;
        this.providerFactory = providerFactory;
    }

    public JobAiAnalysis analyzeJob(UUID jobId, String requestedProvider) {
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        AIProvider provider = (requestedProvider != null && !requestedProvider.isBlank())
            ? providerFactory.getProvider(requestedProvider)
                .orElseThrow(() -> new IllegalArgumentException("Unknown AI provider: " + requestedProvider))
            : providerFactory.getActiveProvider();

        int nextVersion = analysisRepository.findMaxVersionByJobId(jobId) + 1;

        log.info("Initiating AI analysis for job: {} (version={}, provider={})",
            jobId, nextVersion, provider.getProviderName());

        JobAiAnalysis analysis = JobAiAnalysis.createPending(
            jobId,
            nextVersion,
            provider.getProviderName(),
            provider.getDefaultModel(),
            "v1.0"
        );

        AIJobAnalysisRequest request = new AIJobAnalysisRequest(
            job.getId(),
            job.getTitle(),
            job.getCompanyName(),
            job.getDescription(),
            "v1.0"
        );

        try {
            AIJobAnalysisResponse response = provider.analyzeJob(request);
            analysis.complete(response);
            log.info("Successfully completed AI analysis for job: {} (confidence={})",
                jobId, response.confidence());
        } catch (Exception ex) {
            log.error("Failed AI analysis for job: {}", jobId, ex);
            analysis.fail(ex.getMessage() != null ? ex.getMessage() : "Unknown AI processing failure");
        }

        return analysisRepository.save(analysis);
    }

    @Transactional(readOnly = true)
    public JobAiAnalysis getLatestAnalysis(UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }
        return analysisRepository.findLatestByJobId(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("No AI analysis found for job: " + jobId));
    }

    @Transactional(readOnly = true)
    public List<JobAiAnalysis> getAnalysisHistory(UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job not found with ID: " + jobId);
        }
        return analysisRepository.findAllByJobIdOrderByVersionDesc(jobId);
    }
}
