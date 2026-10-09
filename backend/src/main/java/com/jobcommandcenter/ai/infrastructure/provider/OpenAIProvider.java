package com.jobcommandcenter.ai.infrastructure.provider;

import com.jobcommandcenter.ai.domain.AIJobAnalysisRequest;
import com.jobcommandcenter.ai.domain.AIJobAnalysisResponse;
import com.jobcommandcenter.ai.domain.AIProvider;
import com.jobcommandcenter.ai.domain.AIResumeAnalysisRequest;
import com.jobcommandcenter.ai.domain.AIResumeAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Pluggable OpenAI implementation stub for production cloud environments.
 * Activated conditionally when app.ai.provider=OPENAI.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "OPENAI")
public class OpenAIProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAIProvider.class);
    private static final String PROVIDER_NAME = "OPENAI";

    private final String model;
    private final String apiKey;

    public OpenAIProvider(@Value("${app.ai.openai.model:gpt-4o-mini}") String model,
                          @Value("${app.ai.openai.api-key:}") String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String getDefaultModel() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public AIJobAnalysisResponse analyzeJob(AIJobAnalysisRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for job: {}", request.jobId());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public AIResumeAnalysisResponse analyzeResumeFit(com.jobcommandcenter.ai.domain.AIResumeAnalysisRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for resume fit: {}", request.resumeId());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AITailoringResponse generateTailoringSuggestions(com.jobcommandcenter.ai.domain.AITailoringRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for resume tailoring: {}", request.sourceResumeId());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AIEmailClassificationResponse classifyEmail(com.jobcommandcenter.ai.domain.AIEmailClassificationRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for email classification: {}", request.subject());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AIEmailJobExtractionResponse extractJobFromEmail(com.jobcommandcenter.ai.domain.AIEmailJobExtractionRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for job extraction: {}", request.subject());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AIApplicationGuidanceResponse generateApplicationGuidance(com.jobcommandcenter.ai.domain.AIApplicationGuidanceRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for application guidance: {} at {}", request.jobTitle(), request.companyName());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AIInterviewPrepResponse generateInterviewPrep(com.jobcommandcenter.ai.domain.AIInterviewPrepRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for interview prep: {} at {}", request.jobTitle(), request.companyName());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AIAssessmentBriefingResponse generateAssessmentBriefing(com.jobcommandcenter.ai.domain.AIAssessmentBriefingRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for assessment briefing: role={}, platform={}", request.jobTitle(), request.platform());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }

    @Override
    public com.jobcommandcenter.ai.domain.AICompanyDossierResponse generateCompanyDossier(com.jobcommandcenter.ai.domain.AICompanyDossierRequest request) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }
        log.info("Invoking OpenAI API for company dossier: company={}, role={}", request.companyName(), request.jobTitle());
        throw new UnsupportedOperationException("OpenAI remote calls disabled in test profile. Use MOCK provider.");
    }
}


