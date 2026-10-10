package com.jobcommandcenter.ai.domain;

/**
 * Service Provider Interface (SPI) for provider-agnostic AI model integrations.
 * Decouples business logic from specific cloud providers (OpenAI, Anthropic, Gemini, Ollama, Mock).
 */
public interface AIProvider {

    /**
     * Executes structured analysis on a canonical job posting description.
     */
    AIJobAnalysisResponse analyzeJob(AIJobAnalysisRequest request);

    /**
     * Executes qualitative comparison between a structured resume and a job posting.
     */
    AIResumeAnalysisResponse analyzeResumeFit(AIResumeAnalysisRequest request);

    /**
     * Generates section-level resume tailoring suggestions based on job requirements and verified skills.
     */
    AITailoringResponse generateTailoringSuggestions(AITailoringRequest request);

    /**
     * Classifies an incoming email message and calculates confidence score.
     */
    AIEmailClassificationResponse classifyEmail(AIEmailClassificationRequest request);

    /**
     * Extracts structured job opportunities and next steps from email content.
     */
    AIEmailJobExtractionResponse extractJobFromEmail(AIEmailJobExtractionRequest request);

    /**
     * Generates advisory next-action guidance and polite follow-up drafts for an active application.
     */
    AIApplicationGuidanceResponse generateApplicationGuidance(AIApplicationGuidanceRequest request);

    /**
     * Generates grounded interview preparation questions and STAR guidance based on job and verified candidate skills.
     */
    AIInterviewPrepResponse generateInterviewPrep(AIInterviewPrepRequest request);

    /**
     * Generates grounded online assessment briefings, time-management strategies, and study checklists.
     */
    AIAssessmentBriefingResponse generateAssessmentBriefing(AIAssessmentBriefingRequest request);

    /**
     * Generates grounded company technical dossiers, architectural context, and interviewer questions.
     */
    AICompanyDossierResponse generateCompanyDossier(AICompanyDossierRequest request);

    /**
     * Generates grounded career strategy guidance, funnel bottleneck diagnoses, and tactical next steps.
     */
    AIAnalyticsAdvisorResponse generateCareerStrategy(AIAnalyticsAdvisorRequest request);


    /**
     * Identifier of the AI provider (e.g. MOCK, OPENAI, ANTHROPIC, GEMINI, OLLAMA).
     */
    String getProviderName();

    /**
     * Default model identifier for this provider.
     */
    String getDefaultModel();

    /**
     * Returns true if provider is configured and available for requests.
     */
    boolean isAvailable();
}
