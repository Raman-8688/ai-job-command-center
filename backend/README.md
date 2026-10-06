# Backend - AI Job Command Center

## Architecture Overview
The backend is a **Modular Monolith** built on **Java 21+** and **Spring Boot 3.x**. It runs as a single deployable artifact while preserving strict boundary separation across domain modules.

### Planned Modules
The backend monolith will be organized under `com.jobcommandcenter`:
- `user`: User Profile, preferences, and verified skill inventory.
- `job`: Job ingestion, scraping/import parsing, deduplication, and persistence.
- `ai`: AI provider abstraction (`AIProvider`), prompt versioning, structured JSON validation, cost tracking.
- `resume`: Master resume storage, job-specific tailoring, factual validation, and PDF compilation.
- `email`: Gmail API integration, OAuth 2.0 lifecycle, sync scheduler, and parser.
- `application`: Application tracking, status event journal, recruiter directory.
- `interview`: OA and interview tracking, personalized question generator, study briefings.
- `automation`: Automation rules, triggers, actions, and human approval queue.
- `notification`: In-app alerts, desktop notifications, and event broadcasting.
- `analytics`: Search funnel metrics, conversion rates, and skill gap reporting.
- `security`: Spring Security, OAuth 2.0 client, token encryption, and audit logging.
- `common`: Shared domain exceptions, validation utilities, base entities, and API envelope DTOs.

## Engineering Invariants
1. **No Microservices:** No service discovery, Eureka, or distributed RPC.
2. **DTO Isolation:** Never expose JPA entities directly to API controllers.
3. **Factual Integrity:** The AI module interacts with verified profile data; no untrusted attributes may be hallucinated.
4. **Audit Trail:** Key mutations are appended to `audit_logs` without storing sensitive credentials or tokens.

## Setup & Execution (Starting Phase 1)
- Requires JDK 21+ and Maven 3.9+.
- Database: PostgreSQL (configured via `.env` / `application.yml`).
- Flyway manages all schema migrations.
