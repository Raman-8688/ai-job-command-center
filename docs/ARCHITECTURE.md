# Architecture Reference & Module Guide

## 1. High-Level Architecture: Modular Monolith
The **AI Job Command Center** is architected as a **Modular Monolith** in Spring Boot 3.3.4 (Java 21 LTS) and Angular 18, backed by PostgreSQL 17.

Layering convention within each module:
$$\text{HTTP Request} \longrightarrow \text{Controller (api)} \longrightarrow \text{Application Service} \longrightarrow \text{Domain Aggregate} \longrightarrow \text{Repository Contract} \longrightarrow \text{JPA Adapter (infrastructure)} \longrightarrow \text{PostgreSQL}$$

---

## 2. Feature Modules

1. **`common`**: Centralized RFC 7807 problem details, base entities, UUID generators, MDC correlation ID filters (`X-Correlation-ID`), and request logging.
2. **`security`**: Stateless JWT authentication, PBKDF2 / BCrypt password hashing, role-based authorization, and `SecurityUser` UserDetails adapter.
3. **`user`**: User identity, credential verification, and authentication service (`/api/auth/login`, `/api/users/me`).
4. **`profile`**: Candidate profile, target roles, preferred locations, compensation expectations (`/api/profile`).
5. **`skill`**: Canonical skill taxonomy and candidate verified skills catalog (`/api/skills`, `/api/profile/skills`). Anti-hallucination source of truth.
6. **`job`**: Canonical job postings, SHA-256 deduplication hashing, job skill requirements, user job status tracking, and explainable deterministic matching (`/api/jobs`).
7. **`ai`**: Provider-independent `AIProvider` SPI (`MockDeterministicAIProvider`, `OpenAIProvider`), versioned semantic analysis, technology extraction, and qualitative fit scoring (`/api/jobs/{id}/ai-analysis`, `/api/jobs/{id}/ai-fit`).
8. **`resume`**:
   - Master Resume Aggregate: multi-resume management, structured experiences, projects, skills, education, and certifications (`/api/resumes`).
   - Job-Specific Fit Analysis: deterministic and qualitative comparison against canonical jobs (`/api/resumes/{id}/jobs/{jobId}/analysis`).
   - Resume Tailoring & Versioning: versioned drafts per (job, master resume), deterministic keyword coverage, anti-hallucination section suggestions, and review lifecycle (`/api/tailored-resumes`, `/api/resumes/{id}/tailor/{jobId}`).

---

## 3. Database Schema Migrations
- `V1__baseline.sql`: Flyway baseline table structure.
- `V2__user_profile_skills.sql`: User identity, profile, and skill verification schema.
- `V3__jobs_and_matching.sql`: Canonical jobs, deduplication, job skills, and tracking.
- `V4__job_ai_analyses.sql`: AI job analyses and requirement breakdowns.
- `V5__resume_management.sql`: Master resumes and structured child tables.
- `V6__resume_tailoring.sql`: Versioned tailored resumes and section suggestions.
