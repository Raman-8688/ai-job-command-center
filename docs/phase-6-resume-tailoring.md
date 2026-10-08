# Phase 6 — Resume Tailoring, Versioning & Job-Specific Resume Workflow

## 1. Executive Summary

Phase 6 introduces **Resume Tailoring, Resume Versioning & Job-Specific Resume Drafts** to the **AI Job Command Center** modular monolith. It connects the master resume domain (Phase 5), canonical jobs (Phase 3), candidate verified skills (Phase 2), and AI qualitative reasoning (Phase 4) into an interactive human-in-the-loop tailoring workbench with full Angular frontend integration.

### Core Architectural Principles
1. **Master Resume Immutability**:
   The candidate's master resume (`resumes` table) serves as the persistent source of truth. It is **never** automatically rewritten or overwritten during tailoring.
2. **Explicit Lineage & Versioning**:
   Every tailored resume is an explicit versioned draft linked to:
   $$\text{Master Resume} \xrightarrow{\text{Target Job}} \text{Tailored Draft } v_1, v_2, \dots, v_n$$
   Uniqueness is enforced at the database level by `uq_tailored_resume_version (source_resume_id, target_job_id, version)`.
3. **Strict Anti-Hallucination Guard**:
   - The system **never** fabricates candidate experience, technologies, metrics, projects, or credentials.
   - Any skill appearing in the job description that is missing from candidate verified skills is explicitly marked as `[NOT_ENOUGH_EVIDENCE]`.
   - Suggestions are strictly grounded in verified user skills (`UserSkill.isVerified() == true`) and existing master resume text.
4. **Deterministic Keyword Coverage**:
   Keyword coverage and skill matching are computed deterministically by comparing target job required/preferred competencies against resume evidence and verified profile skills.
5. **Human Review Workflow**:
   Drafts move through explicit lifecycle states:
   $$\text{DRAFT} \longrightarrow \text{UNDER\_REVIEW} \longrightarrow \text{APPROVED} \text{ or } \text{REJECTED}$$
   Only candidate-approved drafts are considered ready for external job application use.
6. **Strict Ownership Isolation**:
   Every tailored draft, version, and suggestion is scoped to the authenticated user ID (`SecurityUser.getId()`). Accessing or attempting to mutate another candidate's draft returns RFC 7807 `404 Not Found`.

---

## 2. Architecture & Module Structure

Following the **Feature-Oriented Modular Monolith** and Hexagonal layering:

```text
com.jobcommandcenter
├── common/             # RFC 7807 problem details, correlation ID, logging
├── security/           # JWT filters, token services, SecurityUser
├── user/               # User identity & authentication
├── profile/            # Candidate profile
├── skill/              # Canonical skill catalog & verified candidate skills
├── job/                # Job discovery, normalization & deterministic matching
├── ai/                 # AIProvider SPI, MockDeterministicAIProvider, OpenAIProvider
│   ├── domain/
│   │   ├── AIProvider.java                     # Added generateTailoringSuggestions
│   │   ├── AITailoringRequest.java             # New structured AI input
│   │   ├── AITailoringResponse.java            # New structured AI output
│   │   └── AISuggestionItem.java               # New recommendation record
│   └── infrastructure/provider/
│       ├── MockDeterministicAIProvider.java    # Offline-safe, anti-hallucination implementation
│       └── OpenAIProvider.java                 # Cloud provider stub
└── resume/             # Master Resumes & Tailoring Workflow
    ├── api/
    │   ├── ResumeController.java               # Master resume CRUD (Phase 5)
    │   ├── TailoredResumeController.java       # Tailored draft & review endpoints (Phase 6)
    │   └── dto/
    │       ├── TailoredResumeResponse.java
    │       ├── TailoredResumeSummaryResponse.java
    │       ├── TailoredResumeSuggestionDto.java
    │       ├── UpdateTailoredResumeRequest.java
    │       └── UpdateTailoredResumeStatusRequest.java
    ├── application/
    │   ├── ResumeService.java                  # Master resume orchestration
    │   ├── JobResumeAnalysisService.java       # Fit analysis (Phase 5)
    │   └── ResumeTailoringService.java         # Tailoring orchestration & review logic (Phase 6)
    ├── domain/
    │   ├── Resume.java                         # Master resume aggregate root
    │   ├── TailoredResume.java                 # Tailored resume aggregate root (Phase 6)
    │   ├── TailoredResumeStatus.java           # DRAFT, UNDER_REVIEW, APPROVED, REJECTED
    │   ├── TailoredResumeSuggestion.java       # Section suggestion entity
    │   ├── SectionType.java                    # SUMMARY, EXPERIENCE, PROJECT, SKILLS
    │   └── TailoredResumeRepository.java       # Domain repository contract
    └── infrastructure/
        ├── TailoredResumeJpaEntity.java
        ├── TailoredResumeSuggestionJpaEntity.java
        ├── SpringDataTailoredResumeRepository.java
        └── TailoredResumePersistenceAdapter.java
```

---

## 3. Database Schema (Flyway V6)

Migration script: `backend/src/main/resources/db/migration/V6__resume_tailoring.sql`

```sql
-- 1. Tailored Resumes Table
CREATE TABLE IF NOT EXISTS tailored_resumes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    target_job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    tailored_title VARCHAR(255),
    tailored_summary TEXT,
    keyword_coverage_score NUMERIC(5, 2),
    matched_keywords TEXT,
    missing_keywords TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tailored_resume_version UNIQUE (source_resume_id, target_job_id, version)
);

CREATE INDEX IF NOT EXISTS idx_tailored_resumes_user_id ON tailored_resumes(user_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_source_job ON tailored_resumes(source_resume_id, target_job_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_status ON tailored_resumes(status);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_created_at ON tailored_resumes(created_at DESC);

-- 2. Section Suggestions Table
CREATE TABLE IF NOT EXISTS tailored_resume_suggestions (
    id UUID PRIMARY KEY,
    tailored_resume_id UUID NOT NULL REFERENCES tailored_resumes(id) ON DELETE CASCADE,
    section_type VARCHAR(50) NOT NULL,
    target_item_title VARCHAR(255),
    original_content TEXT,
    suggested_content TEXT NOT NULL,
    rationale TEXT NOT NULL,
    evidence TEXT,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    applied BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tailored_resume_sug_resume_id ON tailored_resume_suggestions(tailored_resume_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resume_sug_section ON tailored_resume_suggestions(section_type);
```

---

## 4. REST API Reference

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/resumes/{resumeId}/tailor/{jobId}` | Initiates tailoring & creates new versioned draft | `201 Created` |
| `GET` | `/api/resumes/{resumeId}/tailored` | Lists all tailored versions for a master resume | `200 OK` |
| `GET` | `/api/jobs/{jobId}/tailored-resumes` | Lists all tailored versions for a specific job | `200 OK` |
| `GET` | `/api/tailored-resumes/{id}` | Retrieves detailed draft with suggestions & coverage | `200 OK` |
| `PUT` | `/api/tailored-resumes/{id}` | Updates draft title and summary content | `200 OK` |
| `PATCH` | `/api/tailored-resumes/{id}/status` | Transitions status (`UNDER_REVIEW`, `APPROVED`, `REJECTED`) | `200 OK` |
| `POST` | `/api/tailored-resumes/{id}/suggestions/{sugId}/apply` | Applies section suggestion into the draft | `200 OK` |
| `DELETE` | `/api/tailored-resumes/{id}` | Deletes a tailored draft | `204 No Content` |

---

## 5. Angular Frontend Integration

The Angular frontend includes:
- **`TailoredResumeWorkbenchComponent`** (`frontend/src/app/features/resume-tailoring/components/tailored-resume-workbench/`):
  - **Keyword Coverage & Metrics**: Gauge displaying score %, matched tags, and missing gap tags.
  - **Section Recommendations Deck**: Diff blocks comparing original vs recommended wording, accompanied by rationale and anti-hallucination evidence pills.
  - **Live Suggestion Application**: One-click "Apply to Draft" button that merges recommendation into the working draft.
  - **Draft Editor & Review Controls**: In-place editor for tailored role title and executive summary, with "Submit for Review", "Approve", and "Reject" buttons.
- **`ResumeTailoringService`** (`frontend/src/app/features/resume-tailoring/services/resume-tailoring.service.ts`):
  - Strongly typed client using Angular `HttpClient` supporting all Phase 6 endpoints.

---

## 6. Verification & Test Coverage

- **Automated Backend Tests**: **81 tests passing (0 failures, 0 errors)**:
  - `TailoredResumeAggregateUnitTest`: Aggregate invariants, status state machine, suggestion application.
  - `ResumeTailoringServiceUnitTest`: Deterministic keyword coverage, version sequencing (v1 -> v2), anti-hallucination labeling (`[NOT_ENOUGH_EVIDENCE]`).
  - `TailoredResumeIntegrationTest`: End-to-end MockMvc testing draft generation, multiple versions, applying suggestions, status transitions, and cross-user security isolation.
  - `PostgreSQLConnectionIntegrationTest`: Validates Flyway V1-V6 execution against local PostgreSQL 17.
- **Frontend Verification**:
  - `npm.cmd run build` executes cleanly (`Application bundle generation complete` in < 4s).
  - Standalone component architecture with unit tests in `resume-tailoring.service.spec.ts` and `app.component.spec.ts`.
