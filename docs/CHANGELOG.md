# Changelog

All notable changes to the **AI Job Command Center** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.10.0] - Phase 9: Interview Management, Preparation & Professional Angular Frontend - 2026-10-09

### Added
- **Interview Domain Model & Aggregate Root:** Implemented `Interview` aggregate root managing interview session lifecycle (`SCHEDULED`, `RESCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), round stages (`INITIAL_SCREEN`, `TECHNICAL_SCREEN`, `SYSTEM_DESIGN`, `BEHAVIORAL_CULTURE`, `HIRING_MANAGER`, `FINAL_ROUND`, `OTHER`), formats (`VIDEO_CALL`, `PHONE_SCREEN`, `ON_SITE`, etc.), and evaluation outcomes (`PENDING`, `PASSED`, `REJECTED`, `STRONG_HIRE`, `HIRE`, `NO_DECISION`).
- **Immutable Timeline Audit Trail:** Created `InterviewEvent` logging all status transitions, reschedulings, notes, and outcome updates chronologically with event types (`SCHEDULED`, `RESCHEDULED`, `COMPLETED`, `CANCELLED`, `STATUS_CHANGED`, `OUTCOME_UPDATED`).
- **Database Schema Migration V9:** Created Flyway `V9__interview_management.sql` creating `interviews`, `interview_events`, and `interview_preparations` tables with foreign keys, check constraints (`scheduled_end_time > scheduled_start_time`), and performance indexes.
- **Job & Candidate Verified Skill Grounded AI Prep:** Extended `AIProvider` SPI with `generateInterviewPrep` method generating tailored, realistic practice questions categorized into `TECH`, `SYSTEM_DESIGN`, `BEHAVIORAL`, and `LEADERSHIP`, complete with STAR model answers and candidate readiness scoring.
- **Candidate Practice Notes & Review Tracking:** Created `InterviewPreparation` entity allowing candidates to save private talking points, record project anecdotes, and mark practice questions as reviewed.
- **Multi-Tenant Security Enforcement:** Full multi-tenant isolation ensuring users can only view, reschedule, or complete their own interviews; foreign queries return `404 Not Found`.
- **Completed Session Immutability Guardrail:** Completed interviews are protected from accidental deletion or subsequent rescheduling (`400 Bad Request`).
- **REST APIs (`/api/interviews`):** Implemented 13 endpoints covering scheduling, filtering, detail inspection, rescheduling, completion with outcome, cancellation, deletion, AI prep generation, custom questions, and dashboard metrics.
- **Professional Angular 18 UI (Matching Reference Layouts):**
  - **Interview Management Cockpit (Images 2 & 3):** Top KPI cards (Upcoming, Completed, Total, Readiness), weekly schedule calendar board with 7-day columns and round-colored cards, round filter chips, meeting join shortcuts, and responsive modal dialogs.
  - **Interview Prep Workspace (Image 1):** ElevateAI-style hero greeting banner, circular SVG candidate readiness score gauge, domain skill progress bars (System Design 92%, Concurrency 88%, Data Consistency 85%, STAR 90%), category pill chips, and expandable STAR model answers.
- **Backend Test Suite Expansion:** Reached 123 passing tests (0 failures, 0 errors) covering state machine invariants, MockMvc REST security, multi-tenant isolation, and end-to-end integration flows.

---

## [0.9.0] - Phase 8: Application Tracking & Lifecycle Management - 2026-10-09

### Added
- **Job Application Domain Model & Aggregate Root:** Implemented `JobApplication` managing application lifecycle states (`DRAFT`, `APPLIED`, `SCREENING`, `ASSESSMENT`, `INTERVIEW`, `OFFER`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `ARCHIVED`) with strict state machine validation rules.
- **Immutable Timeline Audit Log:** Created `JobApplicationEvent` capturing every state transition, note entry, resume link, and stage update chronologically with event types (`CREATED`, `STATUS_CHANGED`, `NOTE_ADDED`, `RESUME_LINKED`, `EMAIL_ASSOCIATED`, `FOLLOW_UP_SCHEDULED`, `REOPENED`).
- **Database Schema Migration V8:** Created Flyway `V8__application_tracking.sql` with `job_applications` table, unique candidate-job constraint (`uq_job_applications_user_job`), `job_application_events` table, and performance indexes.
- **Resume & Tailored Resume Linkages:** Direct reference and user ownership validation connecting applications to canonical master `Resume` and job-specific `TailoredResume` variants.
- **Multi-Tenant Security Enforcement:** Complete user isolation on all endpoints; cross-user application queries return `404 Not Found` to prevent data probing.
- **Advisory AI Next-Step & Follow-Up Guidance:** Extended `AIProvider` SPI with `generateApplicationGuidance` providing deterministic recommended next steps, status inquiry rationales, and customized follow-up draft emails.
- **Funnel Metrics & Due Follow-Up Dashboard:** Added `/api/applications/dashboard-summary` computing active applications, overdue follow-up counts, and stage conversion breakdowns.
- **Angular 18 Application Tracker:** Built `ApplicationTrackerComponent`, `ApplicationTrackingService`, TypeScript interfaces, metrics cards, filter/search toolbar, status badges, slide-over management drawer, and copyable AI draft integration.
- **Backend Test Suite Expansion:** Comprehensive test suite reached 109 passing tests (0 failures, 0 errors) covering state machine invariants, REST security, and end-to-end integration flows.

---

## [0.8.0] - Phase 7: Gmail & Email Integration - 2026-10-08

### Added
- **Gmail OAuth 2.0 Connection Management:** Implemented `EmailConnection` aggregate root supporting Google OAuth authorization URL generation, code-to-token exchange, token refresh, and clean disconnection.
- **Synchronized Inbox Ingestion & Deduplication:** Created `Email` aggregate root and `EmailSyncService` supporting inbox fetching with idempotency on external message IDs (`uq_emails_user_external_message_id`).
- **AI & Deterministic Email Classification:** Automated categorization of incoming emails into lifecycle stages: `APPLICATION_CONFIRMATION`, `INTERVIEW_INVITATION`, `ASSESSMENT`, `OFFER`, `REJECTION`, `NETWORKING_OUTREACH`, `STATUS_UPDATE`, `SPAM_OR_IRRELEVANT`, and `OTHER` with confidence ratings and reasoning.
- **Job Intelligence Extraction & Auto-Creation:** `EmailService.createJobFromEmail` automatically extracts company name, role title, requisition ID, and action items, creates canonical `Job` with `source = JobSource.EMAIL`, adds `UserJob` tracking, and links the email.
- **Bidirectional Job Association:** Created APIs to link, disassociate, and query all emails related to specific jobs (`GET /api/jobs/{jobId}/emails`).
- **Zero Token Leakage & Security Boundary:** Access and refresh tokens are strictly restricted to database persistence and never exposed in REST DTOs (`EmailConnectionResponse`).
- **Flyway Database Migration V7:** Created `V7__gmail_email_integration.sql` creating `email_connections` and `emails` tables with foreign keys and performance indexes.
- **SPI Client Layer:** Implemented `GmailClient` SPI with `MockGmailClient` supporting offline deterministic testing and realistic career email fixtures.
- **REST Endpoints:** Added 14 new endpoints across connection, synchronization, email management, and job linking under `/api/email` and `/api/emails`.
- **API Catalog & Contract Documentation:** Authored `docs/API_CATALOG.md` and `docs/phase-7-gmail-email-integration.md` for seamless frontend collaboration with Bolt.new.

---

## [0.7.0] - Phase 6: Resume Tailoring, Versioning & Job-Specific Resume Workflow - 2026-10-08


### Added
- **Tailored Resume Aggregate & Section Suggestions:** Implemented `TailoredResume` aggregate root and `TailoredResumeSuggestion` entities linking tailored drafts to master resumes and target jobs.
- **Master Resume Immutability:** Strict architectural enforcement ensuring master template resumes in `resumes` table are never modified during tailoring.
- **Resume Versioning Lineage:** Automated version sequencing per `(source_resume_id, target_job_id)` with unique database constraint `uq_tailored_resume_version`.
- **Deterministic Keyword Coverage:** Computes keyword coverage score and tracks matched vs missing requirements against verified candidate skills and resume content.
- **Anti-Hallucination Recommendation Engine:** Section-level suggestions (`SUMMARY`, `SKILLS`, `EXPERIENCE`, `PROJECT`) grounded in verified candidate skills; missing requirements are strictly labeled `[NOT_ENOUGH_EVIDENCE]` with zero invented work history.
- **Human Review Workflow:** Enforces review lifecycle (`DRAFT` -> `UNDER_REVIEW` -> `APPROVED` / `REJECTED`) with suggestion application support.
- **Database Schema Migration V6:** Created Flyway `V6__resume_tailoring.sql` declaring `tailored_resumes` and `tailored_resume_suggestions` tables with indices.
- **REST Endpoints:** Added `/api/resumes/{resumeId}/tailor/{jobId}`, `/api/resumes/{resumeId}/tailored`, `/api/jobs/{jobId}/tailored-resumes`, `/api/tailored-resumes/{id}`, `/api/tailored-resumes/{id}/status`, and `/api/tailored-resumes/{id}/suggestions/{sugId}/apply`.
- **Angular 18 Tailoring Workbench:** Built interactive `TailoredResumeWorkbenchComponent`, `ResumeTailoringService`, keyword coverage progress meters, section suggestions cards with diffs, and review controls.
- **Test Suite:** 81 tests passing with 0 failures and 0 errors across unit, integration, and security layers.

---

## [0.6.0] - Phase 5: Resume Management & Job-Specific Resume Analysis - 2026-10-08

### Added
- **Resume Aggregate & Structured Sections:** Implemented `Resume` aggregate root managing candidate resume metadata and structured child entities: `ResumeExperience`, `ResumeProject`, `ResumeSkill`, `ResumeEducation`, and `ResumeCertification`.
- **Resume Lifecycle & Statuses:** Supported `DRAFT`, `ACTIVE`, and `ARCHIVED` lifecycle states with explicit transition operations.
- **Canonical Skill Catalog Reuse:** Linked resume skills directly to the centralized Phase 2 `Skill` catalog without data duplication.
- **Job-Specific Resume Analysis Engine:** Created `JobResumeAnalysisService` executing deterministic and AI-assisted comparison of candidate resumes against canonical job postings.
- **Skill Gap & Tailoring Insights:** Explicitly detects `strongMatches`, `missingRequiredSkills`, `missingPreferredSkills`, and critically `verifiedSkillsMissingFromResume` (candidate has verified competency not yet showcased on the current resume).
- **Concrete Resume Evidence Mapping:** Automatically extracts concrete section references and verbatim excerpts supporting each matched skill without fabrication.
- **Safe AI Guidance & Anti-Hallucination Guardrails:** AI suggestions are validated against verified candidate skills; unverified recommendations are flagged with `[NOT_ENOUGH_EVIDENCE]`. Zero automated resume modification or fake fact generation.
- **Database Schema Migration V5:** Created Flyway `V5__resume_management.sql` declaring tables `resumes`, `resume_experiences`, `resume_projects`, `resume_skills`, `resume_education`, and `resume_certifications`.
- **REST Endpoints:** Added `/api/resumes` CRUD, activation/archiving endpoints, and `/api/resumes/{resumeId}/jobs/{jobId}/analysis` (both GET and POST).
- **Automated Test Suite:** Created 8 new unit and integration tests covering aggregate lifecycle, section mutations, analysis engine, anti-hallucination checks, and user ownership isolation (75 total tests passing).

---

## [0.5.0] - Phase 4: AI Job Analysis & Fit Scoring - 2026-10-08

### Added
- **AI Analysis Domain & Aggregates:** Implemented `JobAiAnalysis` aggregate root with status lifecycle (`PENDING`, `COMPLETED`, `FAILED`), versioning per job, confidence rating, technology categorization, core/inferred responsibilities, and red flag warnings.
- **Source Truth Immutability:** Enforced architectural rule that canonical job description remains immutable source truth; AI analysis is an explicitly derived entity that never overwrites canonical job text.
- **Anti-Hallucination Grounding:** Enforced candidate skill grounding where only verified skills (`isVerified() == true`) count as possessed skills; AI cannot verify candidate skills or invent competencies.
- **Provider-Independent SPI (`AIProvider`):** Implemented `AIProvider` SPI with `MockDeterministicAIProvider` (offline-safe NLP & keyword analysis), `OpenAIProvider` stub, and `AIProviderFactory` for pluggable provider configuration.
- **Layered Explainable Fit Scoring (`JobAiFitService`):** Combined deterministic Phase 3 match score with AI qualitative technology gap categorization (`matched`, `missing required`, `missing preferred`), interview prep talking points, and qualitative fit tiers (`STRONG_MATCH`, `MODERATE_MATCH`, `WEAK_MATCH`).
- **Database Schema Migration V4:** Created Flyway `V4__job_ai_analyses.sql` declaring tables `job_ai_analyses`, `job_ai_responsibilities`, `job_ai_technologies`, `job_ai_requirements`, and `job_ai_red_flags`.
- **REST Endpoints:** Added `/api/jobs/{id}/ai-analysis` (POST to analyze/re-analyze, GET for latest, GET /history) and `/api/jobs/{id}/ai-fit` (GET explainable candidate fit).
- **Automated Test Suite:** Created 11 new tests across mock provider, versioning, failure isolation, anti-hallucination fit evaluation, and HTTP security/endpoints (67 total tests passing).

---

## [0.4.0] - Phase 3: Job Discovery, Normalization & Matching - 2026-10-08

### Added
- **Canonical Job Domain:** Implemented `Job` aggregate root preserving raw description as immutable source truth, with normalized `WorkMode`, `EmploymentType`, `JobSource`, and `JobStatus`.
- **Deterministic Multi-Tier Deduplication:** Built duplicate prevention based on (source, externalJobId), canonical URL, and SHA-256 composite fingerprinting (`company|title|location`).
- **Normalized Skill Requirements:** Implemented `JobSkill` connecting jobs to the central `Skill` catalog with strict `REQUIRED` vs `PREFERRED` qualification levels.
- **Candidate Job Tracking (`UserJob`):** Established personal candidate interaction tracking (`DISCOVERED`, `SAVED`, `SHORTLISTED`, `IGNORED`, notes) while preserving data privacy.
- **Deterministic Explainable Matching Engine:** Created `JobMatchingService` calculating 6-dimension fit scores (required skills, preferred skills, role title, experience, work mode, location) using verified candidate profile facts without LLM hallucinations.
- **Explainability & Missing Skills Alerting:** Enforced core rule that missing required skills are prominently highlighted in match reasons and cannot be obscured by high overall scores.
- **Database Schema Migration V3:** Created Flyway `V3__jobs_and_matching.sql` defining `jobs`, `job_skills`, and `user_jobs` with foreign keys, indexes, and unique constraints.
- **REST Endpoints:** Added `/api/jobs` CRUD, multi-criteria filtering, pagination, match calculation (`/api/jobs/{id}/match`), and candidate status tracking.
- **Automated Test Suite:** Created 17 new tests covering domain rules, deduplication, match calculations, edge cases, and API flows (56 total tests passing).

---

## [0.3.0] - Phase 2: User Identity, Profile & Verified Skills - 2026-10-08

### Added
- **User Identity & JWT Authentication:** Implemented `User` domain entity, repository abstraction, and persistence adapter; implemented HMAC-SHA512 `JwtTokenService`, `JwtAuthenticationFilter`, and login authentication flow with BCrypt password hashing.
- **Candidate Professional Profile:** Created `Profile` domain entity supporting phone, location, portfolio links, target roles, preferred locations, years of experience, notice period, and company designation.
- **Skill Catalog & Normalized Uniqueness:** Created `Skill` domain entity and catalog management preventing duplicate skill variations via case-insensitive normalization.
- **Anti-Hallucination Verified Skills Engine:** Created `UserSkill` domain entity strictly enforcing core principle 1 (truthfulness) where AI-suggested skills default to unverified (`verified = false`) until explicitly confirmed by the candidate.
- **Database Schema Migration V2:** Created Flyway migration `V2__user_profile_skills.sql` declaring tables `users`, `profiles`, `profile_target_roles`, `profile_preferred_locations`, `skills`, and `user_skills` with foreign keys, indexes, and unique constraints.
- **Integration & Unit Testing Suite:** Created 15 new automated tests covering JWT authentication, principal isolation, profile management, catalog search, anti-hallucination verification, and multi-tenant access control (39 tests total passing).

---

## [0.2.0] - Phase 1: Backend Foundation - 2026-10-07

### Added
- **Spring Boot & Java 21 Foundation:** Configured Maven multi-starter setup with Java 21 LTS, Spring Boot 3.3.4, Spring Web, Validation, Actuator, Security, and JPA.
- **PostgreSQL & Flyway Migrations:** Integrated PostgreSQL 16/17 driver with HikariCP connection pooling; configured Flyway migration lifecycle with initial baseline migration `V1__baseline.sql` initializing `system_metadata`.
- **Environment & Profiles:** Established hierarchical configuration architecture across `application.yml` (base), `application-local.yml` (PostgreSQL local), and `application-test.yml` (in-memory H2 PostgreSQL mode).
- **Spring Security Foundation:** Implemented stateless `SecurityFilterChain` with CORS configuration, public whitelist (`/actuator/health/**`, `/error`, `/api/public/**`), protected API boundaries, and RFC 7807 `AuthenticationEntryPoint` / `AccessDeniedHandler`.
- **Centralized REST Error Handling:** Implemented RFC 7807 Problem Details model (`ErrorResponse`, `ValidationError`) and `@RestControllerAdvice` (`GlobalExceptionHandler`) mapping 400, 401, 403, 404, 405, 409, and 500 status codes with sanitized error messages.
- **Request Correlation & Logging:** Implemented `CorrelationIdFilter` propagating `X-Correlation-ID` to SLF4J MDC and HTTP response headers; implemented `RequestLoggingFilter` logging request latency and status while strictly preventing credential or body leakage.
- **Actuator Health & Observability:** Configured Actuator `/actuator/health` and `/actuator/info` endpoints exposing application and database health.
- **Hermetic Testing Suite:** Authored 24 unit and integration tests across security, validation, error handling, correlation, Actuator, Flyway, and PostgreSQL connectivity.

---

## [0.1.0] - Phase 0: Project Foundation - 2026-10-06

### Added
- **Repository Architecture:** Established Modular Monolith foundation with designated `backend/`, `frontend/`, `docs/`, `infrastructure/`, `scripts/`, and `.github/` directories.
- **Master Plan:** Authored `docs/PROJECT_MASTER_PLAN.md` defining complete system vision, functional and non-functional requirements, data architecture, security posture, and 13-phase roadmap.
- **Architecture Decision Records (ADRs):** Initialized ADR-001 through ADR-008 covering Modular Monolith selection, PostgreSQL persistence, AI Provider abstraction, 3-tier human approval workflow, Gmail API integration, scheduled sync, private repository posture, and local-first development.
- **Engineering Standards & Workflow:** Authored `docs/DEVELOPMENT_WORKFLOW.md` establishing the mandatory 10-step implementation and review cycle.
- **Requirement Specifications:** Created detailed requirements under `docs/requirements/` covering FR-001 to FR-015 and NFR-001 to NFR-012, user stories, use cases, and acceptance criteria.
- **AI Safety & Truthfulness Guardrails:** Documented anti-hallucination policies, prompt versioning strategies, structured JSON schema validations, and cost management under `docs/ai/`.
- **Integration & Security Design:** Specified Gmail OAuth 2.0 least-privilege synchronization, threat model, secret protection, and logging hygiene under `docs/integrations/` and `docs/security/`.
- **Database & API Blueprints:** Designed relational entities, migration protocols, and REST API conventions under `docs/database/` and `docs/api/`.
- **Configuration & Tooling:** Added `.gitignore`, `.env.example`, `docker-compose.yml`, and GitHub issue/PR templates.
