# System Architecture Specification

## 1. Architectural Philosophy
The **AI Job Command Center** is architected as an enterprise-grade **Modular Monolith**. Rather than fragmenting single-user personal workloads across distributed microservices (which introduce network latency, distributed state hazards, orchestration complexity, and high memory usage), the entire backend runs as a single, highly structured Spring Boot application.

```
+-------------------------------------------------------------------------+
|                      CLIENT TIER: Angular 18+ SPA                       |
|   (Dashboard, Pipeline Kanban, Resume Studio, Approval Queue, Inbox)    |
+------------------------------------+------------------------------------+
                                     | HTTPS / REST / JSON
                                     v
+-------------------------------------------------------------------------+
|                APPLICATION TIER: Spring Boot Modular Monolith           |
|                                                                         |
|   [ Web Controllers & DTOs ] ------> [ Security & JWT Filter ]          |
|                                                                         |
|   DOMAIN MODULES:                                                       |
|   - user (Profiles, Verified Skill Matrix)                              |
|   - job (Ingestion, Deduplication, Pipeline State)                      |
|   - ai (Provider Abstraction, Prompt Versioning, Schema Validation)     |
|   - resume (Master Resumes, Factual Tailoring, PDF Generation)          |
|   - email (OAuth 2.0 Gmail Sync, Message Extraction)                    |
|   - application (State Machine, Immutable Event Journal)               |
|   - interview (OA Schedules, Dossier Generation, Question Generator)    |
|   - automation (Rule Evaluation Engine, Approval Queue)                 |
|   - analytics (Funnel Performance, Skill Gap Aggregations)              |
|   - common (Exceptions, Validation, Shared Auditing)                    |
+------------------------------------+------------------------------------+
                                     |
             +-----------------------+-----------------------+
             |                                               |
             v                                               v
+----------------------------+             +------------------------------+
|       DATA TIER:           |             |       EXTERNAL SERVICES:     |
| - PostgreSQL 16 (ACID DB)  |             | - Google Gmail API (OAuth)   |
| - Local Encrypted Storage  |             | - AI Providers (Cloud/Local) |
+----------------------------+             +------------------------------+
```

## 2. Tier Breakdown
1. **Client Tier:** Angular single-page application communicating via REST APIs. Employs strong TypeScript typing matching backend DTO schemas.
2. **Application Tier:** Spring Boot 3 running on OpenJDK 21. Implements strict module boundaries, application-level transactions, and asynchronous task scheduling (`@Async`, `@Scheduled`).
3. **Data Tier:** PostgreSQL 16 utilizing Flyway migrations for schema evolution and JSONB indexing for semi-structured AI payloads.
4. **External Integrations:** Gmail REST API via OAuth 2.0 and swappable LLM providers behind the `AIProvider` interface.
