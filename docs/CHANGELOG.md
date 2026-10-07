# Changelog

All notable changes to the **AI Job Command Center** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
