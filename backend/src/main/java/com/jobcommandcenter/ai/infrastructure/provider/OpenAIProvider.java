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
}
