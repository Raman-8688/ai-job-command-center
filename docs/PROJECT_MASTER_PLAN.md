# PROJECT MASTER PLAN: AI JOB COMMAND CENTER

---

## 1. Project Overview & Objective

**AI Job Command Center** (`ai-job-command-center`) is a private, personal job-search operational platform built to optimize, streamline, and organize the modern software engineering job search.

### 1.1 Business Problem
The current software engineering job market imposes massive repetitive overhead on candidates:
1. **Scattered Portals:** Opportunities are spread across LinkedIn, career sites, job boards, and email alerts.
2. **Generic Tailoring Fatigue:** High-quality applications require custom resumes aligning with specific job descriptions, but manual tailoring is time-consuming and error-prone.
3. **Email Chaos:** Tracking incoming acknowledgments, Online Assessment (OA) invites, recruiter outreach, interview requests, and rejections across an active Gmail account is stressful and prone to missed deadlines.
4. **Follow-Up Inconsistency:** High-value recruiter communications go cold because manual follow-ups are forgotten.
5. **Interview Context Switching:** Preparing for diverse technical rounds requires rapid synthesis of company context, job description tech stacks, and relevant past experience.
6. **Harmful "Spray-and-Pray" Tools:** Existing third-party automation tools spam generic resumes at scale, leading to low response rates, platform bans, and hallucinated experience.

### 1.2 System Objective
To build a **private, truthful, human-in-the-loop Modular Monolith** that maximizes:
$$\text{Relevant Applications} + \text{Application Quality} + \text{Interview Conversion} + \text{Time Saved}$$

The system operates as an intelligent personal assistant that automates safe internal housekeeping, generates high-fidelity tailored drafts, and leaves all final external actions strictly under human discretion.

---

## 2. Core Product Principles

### 2.1 Principle 1: Truthfulness (Zero Hallucination Invariant)
The AI assistant must **NEVER** fabricate:
- Skills or technologies never used
- Years of commercial or academic experience
- Companies, job titles, or dates of employment
- Production claims, scale metrics, or architectural responsibilities
- Certifications, degrees, or personal achievements

All AI-generated resumes, cover letters, and recruiter correspondence must be strictly grounded in the user's **Verified Skill Profile** and verified work history. If a job requires an unknown skill, the system must clearly highlight the gap rather than inventing experience.

### 2.2 Principle 2: Three-Tier Human-in-the-Loop Automation
Every system behavior is assigned an immutable automation level:

| Level | Classification | Behavior & Safeguards | Examples |
|---|---|---|---|
| **Level 1** | **Fully Automatic** | Safe internal operations executed automatically without human intervention. | Email classification, duplicate job detection, JD ingestion & parsing, match scoring, reminder scheduling, analytics rollups. |
| **Level 2** | **AI-Generated Draft** | AI prepares artifacts; human review and explicit approval required to finalize. | Tailored resumes, cover letters, recruiter replies, follow-up messages, interview preparation cheat-sheets. |
| **Level 3** | **External Action** | External actions touching third-party systems; strictly human-initiated. | Sending Gmail emails, submitting job applications, contacting recruiters on LinkedIn/portals. |

### 2.3 Principle 3: Data Privacy & Local Control
The application handles private career history, confidential recruiter discussions, and sensitive OAuth tokens. It is designed to be hosted locally/privately, with no data leaks into public repositories, logs, or unvetted third-party services.

---

## 3. Scope & Target User

### 3.1 Target User
A professional Software Engineer / Technical Lead managing an active, selective career search across multiple companies and platforms.

### 3.2 In-Scope
- Manual and automated job posting ingestion and parsing.
- Verified skill inventory with explicit proficiency and evidence tagging.
- AI-assisted JD analysis, skill gap extraction, and quantitative match scoring.
- Version-controlled Master Resume with automated, truthful job-tailored variant generation.
- Bi-directional Gmail synchronization via official OAuth 2.0 API.
- Intelligent email parsing (identifying OA invites, interviews, rejections, offers).
- Comprehensive application lifecycle tracking with immutable audit event logging.
- Configurable follow-up rule engine with draft generation.
- Personalized interview and OA preparation dossier generation.
- Real-time command center dashboard and funnel analytics.

### 3.3 Out-of-Scope (Forbidden or Future)
- **Forbidden:** Headless mass-application web-crawlers that spam job boards or bypass CAPTCHAs.
- **Forbidden:** Automated unreviewed email sending or auto-submission of applications.
- **Future:** Push-based Gmail Pub/Sub webhooks (scheduled polling is used initially).
- **Future:** Direct audio voice mock-interview simulations.
- **Future:** Autonomous browser automation with anti-bot circumvention.

---

## 4. User Journeys

### Journey 1: Job Discovery, Analysis & Tailored Application
1. **Ingest Job:** User pastes a job URL or raw Job Description (JD) text into the Command Center.
2. **Deduplication Check:** System normalizes company, title, and external URL to prevent duplicate tracking.
3. **AI Analysis:** Level 1 automation parses requirements, required tech stack, seniority, and compares them against the user's Verified Skill Profile.
4. **Scoring & Gaps:** User reviews the overall match score (e.g., 88%), matched strengths, and missing competencies.
5. **Resume Tailoring (Level 2):** User clicks "Generate Tailored Resume". The AI selects relevant verified experiences and emphasizes matching skills without inventing facts.
6. **Review & Approval:** User reviews the generated resume diff, makes manual edits, approves, and downloads the PDF.
7. **Application Submission (Level 3):** User applies on the company portal using the generated PDF. Application status transitions to `APPLIED`.

### Journey 2: Gmail Ingestion, Email Intelligence & OA/Interview Lifecycle
1. **Background Sync (Level 1):** Scheduled task fetches unread job-related emails via Gmail API.
2. **Classification (Level 1):** AI classifies an incoming email as `OA_INVITATION` with confidence 0.96.
3. **State Transition:** The corresponding application status automatically moves to `OA_RECEIVED`.
4. **Dossier Creation (Level 2):** The system generates an OA Preparation briefing (key algorithms, expected platforms, company patterns).
5. **Recruiter Reply Draft (Level 2):** The AI drafts a confirmation email acknowledging receipt.
6. **User Review (Level 3):** The user inspects the draft in the Human Approval Queue, edits if desired, and clicks "Send via Gmail".

---

## 5. System & Module Architecture

### 5.1 Architecture Paradigm: Modular Monolith
The application is structured as a **Modular Monolith** in Java 21+ and Spring Boot. All capabilities run inside a single runtime process, maintaining modularity through strictly separated domain packages:

```
[ Angular SPA Frontend (TypeScript) ]
                 |
                 v REST (JSON)
+-------------------------------------------------------------+
|               SPRING BOOT MODULAR MONOLITH                  |
|                                                             |
|  +----------------+  +----------------+  +----------------+ |
|  |   user         |  |   job          |  |   resume       | |
|  | - Profile      |  | - Ingestion    |  | - Master Resume| |
|  | - Skills       |  | - Analyzer     |  | - Tailoring    | |
|  +----------------+  +----------------+  +----------------+ |
|                                                             |
|  +----------------+  +----------------+  +----------------+ |
|  |   email        |  |  application   |  |   interview    | |
|  | - Gmail Sync   |  | - Tracker      |  | - OA Intel     | |
|  | - Classifier   |  | - Event Log    |  | - Prep Dossier | |
|  +----------------+  +----------------+  +----------------+ |
|                                                             |
|  +----------------+  +----------------+  +----------------+ |
|  |   automation   |  |   ai           |  |   analytics    | |
|  | - Rules Engine |  | - Abstraction  |  | - Funnel KPIs  | |
|  | - Approval Q   |  | - Prompts & Cost| | - Gap Reports  | |
|  +----------------+  +----------------+  +----------------+ |
|                                                             |
|  +----------------+  +----------------+                     |
|  |   security     |  |   common       |                     |
|  | - OAuth Tokens |  | - Exceptions   |                     |
|  | - Audit Log    |  | - DTOs & Util  |                     |
|  +----------------+  +----------------+                     |
+-------------------------------------------------------------+
          |                                  |
          v                                  v
 [ PostgreSQL 16 DB ]               [ Gmail API / AI APIs ]
```

### 5.2 Module Responsibilities & Boundary Enforcement
1. **`user`:** Manages identity, contact details, career goals, and the Verified Skill Profile.
2. **`job`:** Normalizes incoming JDs, computes deduplication hashes, and persists postings.
3. **`ai`:** Provides model-agnostic LLM integration (`AIProvider`), prompt versioning, structured JSON validation, and token cost tracking.
4. **`resume`:** Maintains master resume sections, executes factual tailoring pipelines, and compiles PDF outputs.
5. **`email`:** Manages Gmail OAuth 2.0 tokens, pulls messages, extracts metadata, and triggers classifications.
6. **`application`:** Central state machine tracking job applications from `DISCOVERED` to `OFFER`/`REJECTED`.
7. **`interview`:** Organizes technical interview rounds, OA deadlines, and synthesizes preparation briefs.
8. **`automation`:** Evaluates trigger conditions, executes internal state updates, and enqueues Level 2/3 tasks into the Human Approval Queue.
9. **`analytics`:** Computes response rates, funnel stages, interview conversion, and skill gap frequencies.
10. **`security`:** Enforces authentication, encryption for stored tokens, and audit event recording.
11. **`common`:** Provides centralized exception handlers, base entities, pagination models, and standard API response envelopes.

---

## 6. Data Architecture & Relational Design

The system relies on PostgreSQL 16 with relational integrity, foreign key constraints, and Flyway schema migrations.

### 6.1 Core Relational Schema Overview
- `users`: Core account identity and security credentials.
- `user_profiles`: Contact details, links (GitHub, LinkedIn), target salary, and notice period.
- `skills`: Canonical list of software skills, tools, and categories.
- `user_skills`: User's verified skills with proficiency, years, experience type (`PRODUCTION`, `PERSONAL_PROJECT`, etc.), and verification status.
- `companies`: Company metadata, domain, and industry.
- `jobs`: Job posting details, original text, location, remote status, external URL, and deduplication hash.
- `job_analyses`: AI extraction results, match score (0-100), breakdown scores, and recommendations.
- `resumes`: Master resumes and metadata.
- `resume_versions`: Tailored resume snapshots linked to specific jobs with change diffs and factual validation status.
- `applications`: Application instance connecting a user, job, and resume version.
- `application_events`: Immutable audit trail of every status transition (`DISCOVERED` -> `APPLIED` -> `OA` -> `INTERVIEW`).
- `emails`: Stored email metadata and sanitized content snippets.
- `email_classifications`: AI classification results, extracted dates/deadlines, and confidence scores.
- `interviews` & `oas`: Scheduled technical interviews and assessment details.
- `approval_queue`: Pending Level 2 and Level 3 actions awaiting human confirmation.
- `ai_requests` & `ai_responses`: Complete audit trail of LLM interactions, prompts, costs, and tokens.
- `audit_logs`: Security and operational audit trail.

---

## 7. AI Architecture & Anti-Hallucination Strategy

### 7.1 Provider Abstraction
Business logic never communicates directly with a specific cloud AI SDK. A decoupled interface is used:
```java
public interface AIProvider {
    AIResponse generate(AIRequest request);
    boolean isAvailable();
    String getProviderName();
}
```
Implementations include `OpenAIProvider`, `AnthropicProvider`, `GeminiProvider`, and local `OllamaProvider`.

### 7.2 Structured Output & JSON Validation
All AI operations return strictly validated JSON schemas. Every raw output undergoes a three-step validation pipeline:
1. **Syntactic JSON Check:** Parsing and fixing trailing commas or markdown wraps.
2. **Schema & Bean Validation:** Mapping to strongly typed DTOs using Java Bean Validation.
3. **Factual Grounding Check:** Cross-referencing mentioned technologies against `user_skills`. Any unsupported claim causes the pipeline to reject the output or mark the item as `UNVERIFIED`.

---

## 8. Gmail Integration Strategy

### 8.1 Least-Privilege OAuth 2.0
- Uses official Google Cloud OAuth credentials.
- Scopes are restricted to:
  - `https://www.googleapis.com/auth/gmail.readonly` (Read and classify)
  - `https://www.googleapis.com/auth/gmail.compose` (Create drafts for human review)
  - `https://www.googleapis.com/auth/gmail.send` (Only triggered on explicit Level 3 user action)
- No user passwords or unencrypted refresh tokens are stored. Tokens are encrypted at rest using AES-256-GCM.

### 8.2 Scheduled Polling Architecture
Phase 7 implements scheduled batch polling (e.g., every 15 minutes) with incremental history IDs (`historyId`), avoiding complex push webhook infrastructure during initial phases.

---

## 9. Non-Functional Requirements (NFRs)

- **Performance:** Dashboard API responses < 200ms; AI analysis processed asynchronously with task status polling.
- **Reliability:** Idempotent email processing; duplicate emails or duplicate webhook triggers produce zero redundant database entries.
- **Security:** Zero secrets in version control; OWASP Top 10 compliance; strict CORS policies; sanitized logs.
- **Observability:** Structured JSON logging, correlation IDs (`X-Correlation-ID`) across requests, and Spring Boot Actuator health checks.

---

## 10. Development Roadmap (Phases 0 to 12)

| Phase | Title | Core Objective | Primary Deliverables |
|---|---|---|---|
| **Phase 0** | **Project Foundation** | Establish repository, ADRs, engineering rules, and complete docs. | Architecture docs, ADR 001-008, .env.example, docker-compose. *(Complete)* |
| **Phase 1** | **Backend Foundation** | Spring Boot runtime, PostgreSQL, Flyway, security, error handling. | Base project skeleton, Flyway setup, GlobalExceptionHandler, Actuator, Security foundation. *(Complete - see [docs/phase-1-backend-foundation.md](phase-1-backend-foundation.md))* |
| **Phase 2** | **User Profile & Skills** | Verified skill inventory and anti-hallucination source of truth. | User profile CRUD, verified skill tagging, category management, JWT auth, Flyway V2. *(Complete - see [docs/phase-2-user-profile-skills.md](phase-2-user-profile-skills.md))* |
| **Phase 4** | **AI Job Analysis** | Semantic extraction, seniority detection, explainable fit scoring. | `AIProvider` SPI, `MockDeterministicAIProvider`, Flyway V4, fit evaluator. *(Complete - see [docs/phase-4-ai-job-analysis.md](phase-4-ai-job-analysis.md))* |
| **Phase 5** | **Resume Management & Analysis** | Structured multi-resume domain, job-specific analysis, gap detection, evidence extraction. | `Resume` aggregate, structured sections, Flyway V5, `JobResumeAnalysisService`, evidence mapping. *(Complete - see [docs/phase-5-resume-management.md](phase-5-resume-management.md))* |
| **Phase 6** | **Resume Tailoring & Versioning** | Versioned drafts per (job, resume), keyword coverage, section suggestions, human review workflow, Angular workbench. | `TailoredResume` aggregate, version sequencing, Flyway V6, `TailoredResumeWorkbenchComponent`, review states. *(Complete - see [docs/phase-6-resume-tailoring.md](phase-6-resume-tailoring.md))* |
| **Phase 7** | **Gmail Integration & Processing** | OAuth 2.0 connection, inbox sync, AI classification, and job auto-extraction. | Gmail client SPI, OAuth flow, Flyway V7, `EmailService`, email classification & job linking. *(Complete - see [docs/phase-7-gmail-email-integration.md](phase-7-gmail-email-integration.md))* |

| **Phase 8** | **Application Tracking & Lifecycle** | Dedicated job application lifecycle, state machine transitions, timeline audit log, resume linkages, funnel metrics, and AI next-step guidance. | `JobApplication` aggregate root, `JobApplicationEvent` immutable timeline, Flyway V8, `ApplicationTrackerComponent`, AI guidance SPI. *(Complete - see [docs/phase-8-application-tracking.md](phase-8-application-tracking.md))* |
| **Phase 9** | **Interview Management & Preparation** | End-to-end interview lifecycle management, weekly calendar scheduling cockpit, and grounded AI simulation & STAR prep workspace. | `Interview` aggregate root, `InterviewEvent` immutable audit timeline, `InterviewPreparation` question notes, Flyway V9, `InterviewCockpitComponent`, `InterviewPrepWorkspaceComponent`, AI prep SPI. *(Complete - see [docs/phase-9-interview-management.md](phase-9-interview-management.md))* |
| **Phase 10** | **Interview & OA Intel** | Assessment deadlines, technical dossiers, interview prep. | OA tracker, technical question builder, company briefing generator. |
| **Phase 11** | **Analytics & KPIs** | Conversion funnel metrics, response rates, and skill gap insights. | Analytics service, stage conversion charts, source effectiveness. |
| **Phase 12** | **Hardening & Security** | End-to-end security audit, backup automation, and resilience checks. | Security audit, disaster recovery drills, performance benchmarks. |

---

## 11. Definition of Done (DoD)

A development phase or feature is marked complete if and only if:
1. **Design Alignment:** Matches the specifications in `docs/` and architectural ADRs.
2. **Truthfulness Checked:** No path permits AI hallucination or unverified claims.
3. **Human Control:** Level 2/3 actions are intercepted by the approval queue.
4. **Automated Tests:** Unit tests pass with clean assertions; edge cases and malformed inputs are covered.
5. **No Secret Leaks:** Verified clean of credentials, OAuth tokens, and sensitive data.
6. **Documentation Updated:** `CHANGELOG.md` and related docs are updated to reflect the new state.

---

## 12. Risks & Mitigations

| Risk | Impact | Mitigation Strategy |
|---|---|---|
| **AI Hallucination in Resumes** | High (Reputational damage with employers) | Strict factual validation interceptor comparing claims to `user_skills`. |
| **Accidental Email Sending** | High (Unintended communication) | Strict Level 3 classification; sending requires explicit human confirmation. |
| **Gmail OAuth Token Revocation** | Medium (Sync disruption) | Graceful error handling, status warning in UI, easy re-auth flow. |
| **Job Board Anti-Scraping Changes** | Medium (Ingestion failure) | Robust manual paste fallback with clean markdown extraction. |
| **AI API Cost Runaway** | Low-Medium (Cloud billing spikes) | Token usage tracking, prompt optimization, and local LLM option (Ollama). |
