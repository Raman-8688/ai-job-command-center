# Changelog

All notable changes to the **AI Job Command Center** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
