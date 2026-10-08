# Phase 3 — Job Discovery, Normalization & Matching

## 1. Executive Summary

Phase 3 builds the canonical job intelligence foundation of the **AI Job Command Center** modular monolith. It introduces:
1. **Canonical Job Domain**: An aggregate root representing real-world job opportunities with raw descriptions preserved immutably as source truth facts.
2. **Deterministic Multi-Tier Deduplication**: Prevents duplicate postings across source-specific external IDs, canonical job URLs, and normalized composite fingerprints (`SHA-256(company|title|location)`).
3. **Normalized Skill Requirements**: Links canonical jobs to the master `Skill` catalog created in Phase 2, strictly categorizing skills into `REQUIRED` vs `PREFERRED`.
4. **Candidate Job Interaction Tracking (`UserJob`)**: Personal candidate tracking (`DISCOVERED`, `SAVED`, `SHORTLISTED`, `IGNORED`, candidate notes) isolated from global job records.
5. **Deterministic & Explainable Matching Engine (`JobMatchingService`)**: Evaluates candidate fit across 6 distinct dimensions using verified facts from Phase 2 (`Profile` and verified `UserSkill` claims). Zero LLM hallucinations are introduced.
6. **Core Explainability Principle**: Missing `REQUIRED` skills are explicitly exposed and prominently alerted; a high overall match percentage can never obscure missing mandatory requirements.

---

## 2. Architecture & Layering

Following the **Feature-Oriented Modular Monolith** architecture:

```text
com.jobcommandcenter
├── common/             # Cross-cutting error responses, correlation ID, logging
├── security/           # JWT filters, token services, SecurityUser UserDetails adapter
├── user/               # User Identity & Authentication module
├── profile/            # Candidate Profile module
├── skill/              # Skill Catalog & Candidate Skills module
└── job/                # Job Discovery, Normalization & Matching module
    ├── api/            # JobController, request/response DTO records, PageResponse
    ├── application/    # JobService, JobMatchingService
    ├── domain/         # Job, JobSkill, UserJob, JobStatus, JobSource, WorkMode,
    │                   # EmploymentType, SkillRequirementType, UserJobStatus,
    │                   # JobMatchResult, JobSearchCriteria, JobRepository, UserJobRepository
    └── infrastructure/ # JobJpaEntity, JobSkillJpaEntity, UserJobJpaEntity,
                        # SpringData repositories, JobPersistenceAdapter, UserJobPersistenceAdapter
```

### Dependency Rules:
- **API** depends strictly on **Application** (controllers remain thin orchestrators).
- **Application** coordinates domain aggregates and repository interfaces.
- **Domain** contains pure POJOs, business rules, invariants, and repository interfaces without Spring/JPA dependencies.
- **Infrastructure** implements domain repository interfaces via JPA/Spring Data adapters.

---

## 3. Database Schema (Flyway V3)

Migration script: `db/migration/V3__jobs_and_matching.sql`

```sql
-- 1. Canonical Jobs Table
CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    external_job_id VARCHAR(150),
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_website VARCHAR(500),
    job_url VARCHAR(1000),
    description TEXT NOT NULL,
    location VARCHAR(255),
    work_mode VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',
    employment_type VARCHAR(50) NOT NULL DEFAULT 'FULL_TIME',
    experience_min_years NUMERIC(4, 1),
    experience_max_years NUMERIC(4, 1),
    salary_min NUMERIC(12, 2),
    salary_max NUMERIC(12, 2),
    salary_currency VARCHAR(10),
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_url VARCHAR(1000),
    posted_at TIMESTAMP WITH TIME ZONE,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    application_deadline TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    deduplication_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_source_ext_id ON jobs(source, external_job_id);
CREATE INDEX idx_jobs_dedup_hash ON jobs(deduplication_hash);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_company ON jobs(company_name);
CREATE INDEX idx_jobs_work_mode ON jobs(work_mode);
CREATE INDEX idx_jobs_created_at ON jobs(created_at DESC);

-- 2. Job Skills Table
CREATE TABLE job_skills (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    requirement_type VARCHAR(50) NOT NULL DEFAULT 'REQUIRED',
    years_experience_required NUMERIC(4, 1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_skills_job_skill UNIQUE (job_id, skill_id)
);

CREATE INDEX idx_job_skills_job_id ON job_skills(job_id);
CREATE INDEX idx_job_skills_skill_id ON job_skills(skill_id);
CREATE INDEX idx_job_skills_req_type ON job_skills(requirement_type);

-- 3. Candidate Jobs Table (User Tracking & Isolation)
CREATE TABLE user_jobs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'DISCOVERED',
    notes TEXT,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_jobs_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX idx_user_jobs_user_id ON user_jobs(user_id);
CREATE INDEX idx_user_jobs_job_id ON user_jobs(job_id);
CREATE INDEX idx_user_jobs_status ON user_jobs(status);
```

---

## 4. Deduplication Strategy

To prevent redundant duplicate job entries from multiple discovery channels, the system evaluates three deterministic levels before persistence:

1. **Source & External ID**: If both `source` and `externalJobId` exist, an exact match rejects creation (`409 Conflict`).
2. **Canonical URL**: Exact URL match on `jobUrl` rejects creation (`409 Conflict`).
3. **Normalized Fingerprint Hash (`SHA-256`)**:
   ```java
   String rawKey = normCompany + "|" + normTitle + "|" + normLocation;
   String hash = SHA256(rawKey);
   ```
   Ensures that postings with identical company, job title, and location (regardless of case, whitespace, or minor URL parameter alterations) are identified as duplicates.

---

## 5. Deterministic Matching Engine

The matching engine in `JobMatchingService` operates deterministically without LLMs or probabilistic guessing.

### The 6 Dimension Weights:

| Dimension | Weight | Scoring Logic |
|---|---|---|
| **Required Skills** | **35%** | `(matchedRequired / totalRequired) * 100`. If job has no required skills: `100`. |
| **Preferred Skills** | **15%** | `(matchedPreferred / totalPreferred) * 100`. If job has no preferred skills: `100`. |
| **Title / Target Role** | **20%** | Exact/substring match against profile `targetRoles`: `100`. Keyword overlap: `70-85`. Mismatch: `30`. |
| **Experience Compatibility** | **15%** | Candidate years within [min, max]: `100`. Slightly below (<= 1 yr): `70`. Far below (> 1 yr): `25`. Exceeds max: `85`. Range unspecified: `100`. |
| **Work Mode Compatibility** | **10%** | Exact match (`REMOTE` == `REMOTE`): `100`. Open preference: `100`. Candidate `REMOTE` vs Job `ONSITE`/`HYBRID`: `20`. |
| **Location Compatibility** | **5%** | Job `REMOTE`: `100`. Match with candidate home or `preferredLocations`: `100`. Mismatch: `30`. |

$$\text{OverallScore} = \text{round}(0.35 \times S_{\text{req}} + 0.15 \times S_{\text{pref}} + 0.20 \times S_{\text{title}} + 0.15 \times S_{\text{exp}} + 0.10 \times S_{\text{mode}} + 0.05 \times S_{\text{loc}})$$

### Strict Anti-Hallucination & Explainability Guarantees:
1. **Verified Data Only**: The matching engine only considers `UserSkill` entries with `verified == true`. Unverified claims (including raw `AI_SUGGESTED` items from Phase 2) are excluded from matched skills.
2. **Missing Required Skills Visibility**: If any required skill is absent, it is populated in `missingRequiredSkills` and prepended at index 0 of `matchReasons`:
   ```text
   ATTENTION: Missing 2 required skill(s): [AWS, Kubernetes]
   ```
   No high score can obscure missing mandatory requirements.

---

## 6. API Endpoints

All endpoints require JWT Bearer authentication.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/jobs` | Manually ingest new canonical job with required/preferred skills |
| `GET` | `/api/jobs` | Search and filter jobs (`query`, `status`, `source`, `workMode`, `company`, `location`, `page`, `size`) |
| `GET` | `/api/jobs/{id}` | Retrieve job details by ID with resolved skill names |
| `PUT` | `/api/jobs/{id}` | Update canonical job posting and skill requirements |
| `DELETE` | `/api/jobs/{id}` | Delete job posting (cascades to skills and user tracking) |
| `GET` | `/api/jobs/{id}/match` | Evaluate deterministic match score for authenticated candidate |
| `GET` | `/api/jobs/matches` | Evaluate and list matches for all active jobs sorted by score |
| `PUT` | `/api/jobs/{id}/user-status`| Update candidate interaction status (`DISCOVERED`, `SAVED`, `SHORTLISTED`, `IGNORED`) |

---

## 7. Testing & Quality Verification

Automated test suite (`mvn clean test`) executes **56 tests** across unit and integration levels with **100% pass rate**:
- `JobUnitTest`: Entity validation, immutable raw description, and deterministic deduplication hashing.
- `JobMatchingServiceUnitTest`: All-skills matched, missing required skills prominent warning, unverified skill exclusion, work mode mismatch penalty, experience boundary penalties, and location matching.
- `JobIntegrationTest`: Job creation, multi-tier deduplication conflict enforcement (409), search filtering, pagination, candidate matching, unauthenticated access rejection (401), and personal status updates.
- `DatabaseFlywayIntegrationTest` & `PostgreSQLConnectionIntegrationTest`: V1, V2, and V3 migrations validated against both in-memory H2 and live PostgreSQL 17.
