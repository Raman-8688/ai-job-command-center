# Module Responsibilities & Contracts

| Module | Core Responsibility | Public Services Exposed | Events Published |
|---|---|---|---|
| **`user`** | Candidate identity, links, preferences, and verified skill matrix. | `UserProfileService`, `SkillQueryService` | `SkillUpdatedEvent` |
| **`job`** | Job ingestion, URL parsing, deduplication, status tracking. | `JobCommandService`, `JobQueryService` | `JobCreatedEvent`, `JobStatusChangedEvent` |
| **`ai`** | LLM provider abstraction, prompt rendering, schema validation, token tracking. | `AIService`, `PromptManager` | `AIRequestCompletedEvent` |
| **`resume`** | Master resume maintenance, job-tailoring engine, factual verification, PDF generation. | `ResumeTailoringService`, `ResumeExportService` | `ResumeGeneratedEvent` |
| **`email`** | Gmail OAuth 2.0 client, sync scheduler, message parsing, draft creation. | `GmailSyncService`, `EmailDraftService` | `EmailIngestedEvent`, `EmailClassifiedEvent` |
| **`application`** | Application lifecycle management, recruiter directory, event log. | `ApplicationService`, `ApplicationEventService` | `ApplicationStateChangedEvent` |
| **`interview`** | Technical round tracking, OA deadlines, study dossiers, prep prompts. | `InterviewService`, `OAPreparationService` | `InterviewScheduledEvent` |
| **`automation`** | Automation rule engine, triggers, actions, and Human Approval Queue. | `RuleEngineService`, `ApprovalQueueService` | `ApprovalActionExecutedEvent` |
| **`analytics`** | Metrics computation, funnel conversions, skill gap aggregations. | `AnalyticsService`, `MetricsQueryService` | None |
| **`security`** | User authentication, token encryption/decryption, audit trail. | `TokenVaultService`, `AuditLogService` | `SecurityAuditEvent` |
| **`common`** | Base exceptions, API envelopes, validation utilities, date formatters. | Shared utilities across modules | None |
