# AI Job Command Center

> A private, AI-assisted personal job-search command center built as a high-discipline **Modular Monolith** in Java 21+ / Spring Boot and Angular.

[![Architecture: Modular Monolith](https://img.shields.io/badge/Architecture-Modular%20Monolith-blue.svg)](docs/architecture/modular-monolith-architecture.md)
[![Phase: 0 Foundation](https://img.shields.io/badge/Status-Phase%200%20(Foundation)-yellow.svg)](docs/PROJECT_MASTER_PLAN.md)
[![Security: Private & Human-in-the-Loop](https://img.shields.io/badge/Security-Human%20Approval%20Required-green.svg)](docs/security/security-architecture.md)

---

## 1. Project Overview

**AI Job Command Center** (`ai-job-command-center`) is a private, production-grade personal productivity and intelligence platform designed to manage and optimize real-world software engineering job searches.

This application is **NOT a portfolio or demo project**. It is an engineering-grade system built to manage genuine professional career opportunities, track live job pipelines, classify confidential Gmail correspondence via official OAuth 2.0 APIs, analyze job descriptions, and generate tailored, factually grounded resume variants.

### Core Value Metric
The primary goal is **NOT** to blindly mass-apply to hundreds of postings.  
The system optimizes for:
$$\text{Quality of Applications} + \text{Relevance} + \text{Interview Conversion Rate} + \text{Time Saved}$$

---

## 2. Core Product Principles

### Principle 1 — Strict Truthfulness (Anti-Hallucination)
The system operates under a zero-fabrication invariant. AI models must **NEVER** invent skills, years of experience, companies, production claims, certifications, or projects. Tailored resumes and communications are strictly grounded in the user's **Verified Skill Profile** and verified history. If information is missing, the system marks it as `UNKNOWN` or asks the user.

### Principle 2 — Human-in-the-Loop (3-Level Automation)
Automation is strictly governed by risk tiers:
- **Level 1 — Fully Automatic (Safe Internal Actions):** Email classification, duplicate job detection, JD parsing & scoring, database indexing, reminder scheduling, metrics aggregation.
- **Level 2 — AI-Generated Draft (Human Approval Required):** Resume tailoring, cover letters, recruiter replies, follow-up messages, interview preparation briefings.
- **Level 3 — External Action (Explicit Human Execution):** Sending emails, submitting applications, contacting recruiters. **No blind mass-application botting.**

### Principle 3 — Privacy & Data Isolation
Job applications and email inboxes contain sensitive personal and professional data. All credentials remain local; OAuth tokens are encrypted at rest; secrets and raw personal identifiers are excluded from logs and version control.

---

## 3. Architecture & Technology Stack

The platform is architected as a **Modular Monolith** running within a single Spring Boot runtime, organized into strictly bounded domain modules without distributed microservice overhead (no Eureka, Kafka, or API gateways).

```
                      +-----------------------------+
                      |   Angular 18+ SPA Client    |
                      +--------------+--------------+
                                     | REST / JSON
                                     v
+--------------------------------------------------------------------------+
|                  SPRING BOOT MODULAR MONOLITH (Java 21+)                 |
|                                                                          |
|  +-------------+  +-------------+  +-------------+  +-----------------+  |
|  | UserProfile |  | Job Module  |  |   Resume    |  |  Application    |  |
|  +-------------+  +-------------+  +-------------+  +-----------------+  |
|  +-------------+  +-------------+  +-------------+  +-----------------+  |
|  | Email Module|  | Interview/OA|  |  Analytics  |  | Human Approval  |  |
|  +-------------+  +-------------+  +-------------+  +-----------------+  |
|                                                                          |
|  +---------------------------+       +--------------------------------+  |
|  |    AI Provider Service    |       |      Automation Engine         |  |
|  | (OpenAI / Claude / Ollama)|       |   (Triggers / Rules / Actions) |  |
|  +---------------------------+       +--------------------------------+  |
+----------------------+-------------------------------+-------------------+
                       |                               |
                       v                               v
             +--------------------+         +--------------------+
             | PostgreSQL 16 (DB) |         | Gmail API (OAuth)  |
             +--------------------+         +--------------------+
```

- **Backend:** Java 21+, Spring Boot, Spring Web, Spring Data JPA, Hibernate, Spring Security, Flyway migrations, Maven.
- **Frontend:** Angular, TypeScript, Angular Material / clean component design.
- **Database:** PostgreSQL with relational integrity, schema migrations, and indexing.
- **AI Abstraction:** Unified `AIProvider` interface supporting Cloud (OpenAI, Anthropic, Gemini) and Local (Ollama, vLLM) models.
- **Email:** Google Gmail REST API via OAuth 2.0 (least-privilege scopes).
- **Storage:** Local encrypted file storage with an interface abstraction for future object stores.

---

## 4. Repository Structure

```text
ai-job-command-center/
├── .github/
│   ├── workflows/           # CI pipelines
│   ├── ISSUE_TEMPLATE/      # Bug, feature, improvement templates
│   └── pull_request_template.md
├── backend/                 # Spring Boot Modular Monolith
│   └── README.md
├── frontend/                # Angular SPA frontend
│   └── README.md
├── docs/                    # Exhaustive architecture & engineering docs
│   ├── PROJECT_MASTER_PLAN.md
│   ├── CHANGELOG.md
│   ├── DEVELOPMENT_WORKFLOW.md
│   ├── requirements/        # FRs, NFRs, use cases, acceptance criteria
│   ├── architecture/        # Monolith design, boundaries, data flows
│   ├── database/            # Schema definitions, ER diagrams, data dictionary
│   ├── api/                 # REST conventions, error standards, endpoints
│   ├── ai/                  # Prompts, anti-hallucination, validation, costs
│   ├── integrations/        # Gmail OAuth, rate limits, webhooks/sync
│   ├── security/            # Threat model, token vault, secret management
│   ├── testing/             # Unit, integration, AI evaluation, E2E
│   ├── deployment/          # Local setup, Docker Compose, backups
│   └── decisions/           # Architecture Decision Records (ADR 001 - 008)
├── infrastructure/          # Container configs and provisioning assets
├── scripts/                 # Maintenance, migration, and developer utilities
├── docker-compose.yml       # Local PostgreSQL service
├── .env.example             # Configuration template
├── .gitignore               # Strict ignore rules for Java, Node, and secrets
└── README.md                # Root project overview
```

---

## 5. Development Roadmap

The project is built incrementally across 13 planned phases:

- [x] **Phase 0:** Project Foundation, Repository Structure & Documentation *(Current)*
- [ ] **Phase 1:** Backend Foundation, Configuration & Security Framework
- [ ] **Phase 2:** User Profile & Verified Skill Management
- [ ] **Phase 3:** Job Ingestion, Normalization & Deduplication
- [ ] **Phase 4:** AI Job Analysis & Fit Scoring Engine
- [ ] **Phase 5:** Resume Customization & Rendering Engine
- [ ] **Phase 6:** Angular Command Center Dashboard
- [ ] **Phase 7:** Gmail Integration & Scheduled Synchronization
- [ ] **Phase 8:** Email Intelligence & Draft Assistant
- [ ] **Phase 9:** Application State Machine & Automation Queue
- [ ] **Phase 10:** Interview & Online Assessment Preparation Engine
- [ ] **Phase 11:** Job Search Analytics & Conversion Metrics
- [ ] **Phase 12:** Production Hardening, Audit & Security Review

Full details for each phase are specified in [PROJECT_MASTER_PLAN.md](docs/PROJECT_MASTER_PLAN.md).

---

## 6. Security & Credential Hygiene Warning

> [!CAUTION]
> **NEVER COMMIT CREDENTIALS OR PERSONAL INFORMATION.**
> - Do not commit `.env`, `*.pem`, `*.jks`, or Google client secret JSON files.
> - Gmail tokens are strictly kept out of Git and must be encrypted at rest.
> - Application logs must never write OAuth access/refresh tokens, raw email contents, or user passwords.

---

## 7. Local Environment Setup (Phase 0)

To inspect and prepare the workspace:

1. **Verify Prerequisites:**
   - Git 2.40+
   - OpenJDK 21+
   - Node.js 20+ & npm 10+
   - Docker & Docker Compose

2. **Copy Environment Template:**
   ```bash
   cp .env.example .env
   ```

3. **Start Local Database:**
   ```bash
   docker-compose up -d postgres
   ```

Refer to [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) for contribution rules and quality gates.
