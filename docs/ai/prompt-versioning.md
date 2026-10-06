# Prompt Versioning Strategy

## 1. Zero Hardcoding Policy
Large prompts must **never** be hardcoded as ad-hoc strings scattered across Java services. All prompts are managed as versioned resources.

## 2. Naming & Storage Convention
Prompts reside in `backend/src/main/resources/prompts/` using explicit semantic versions:
- `JOB_ANALYZER_V1.st`
- `EMAIL_CLASSIFIER_V1.st`
- `RESUME_OPTIMIZER_V1.st`
- `EMAIL_REPLY_V1.st`
- `FOLLOWUP_GENERATOR_V1.st`
- `INTERVIEW_PREP_V1.st`
- `OA_PREP_V1.st`

## 3. Metadata Logging
Every AI request and resulting response logged in `ai_requests` records:
- `prompt_name` (e.g., `JOB_ANALYZER`)
- `prompt_version` (e.g., `1.0.0`)
- `model_name` (e.g., `gpt-4o-mini`)
- `temperature` (e.g., `0.1` for parsing, `0.4` for drafting)
- `latency_ms`
- `input_tokens` / `output_tokens`
- `cost_estimate_usd`
