# AI Agent Context & Durable Memory

## Project: AI Job Command Center
**Repository:** `Raman-8688/ai-job-command-center`  
**Architecture:** Modular Monolith (Spring Boot 3.3.4, Java 21, PostgreSQL 17, Flyway, Angular 18)

---

## 1. Architectural Principles & Rules
1. **Real Personal Job-Search System**:
   This is a real-world, long-term personal productivity and automation application. It is **not** a toy or portfolio demo.
2. **Modular Monolith**:
   Do **not** convert to microservices. All features reside in feature-oriented packages under `com.jobcommandcenter.*`.
3. **Zero Fabrication / Anti-Hallucination**:
   - The system must **never** invent skills, work history, projects, credentials, or metrics.
   - If a target job mentions an unverified skill, it must be flagged with `[NOT_ENOUGH_EVIDENCE]`. It must **never** be turned into claimed experience.
   - Verified candidate skills (`UserSkill.isVerified() == true`) and explicit master resume text are the sole ground truth.
4. **Master Resume Immutability**:
   The master resume is **never** automatically rewritten or overwritten. Tailored resumes are versioned drafts linked to the master resume and target job (`(sourceResumeId, targetJobId, version)`).
5. **Human Control & Review**:
   Level 2 and 3 actions (e.g., job applications, email sending, resume finalization) require human approval (`APPROVED` status).
6. **Git Operations**:
   Agents must **never** run `git commit`, `git push`, merge branches, or create PRs. The human developer handles Git operations.
7. **Database Safety**:
   Never run `DROP TABLE`, truncate, or rewrite Flyway migrations. Flyway migrations (`V1` to `V6`) are immutable.

---

## 2. Completed Phases

- **Phase 0 — Project Foundation**: Repository baseline, ADRs, initial docs.
- **Phase 1 — Backend Foundation**: Spring Boot runtime, PostgreSQL setup, Flyway, RFC 7807 problem details, correlation ID, logging.
- **Phase 2 — User Identity, Profile & Verified Skills**: JWT auth, profile management, canonical skill catalog, anti-hallucination verified skills (`V2__user_profile_skills.sql`).
- **Phase 3 — Job Discovery, Normalization & Matching**: Canonical Job domain, deduplication hashing, JobSkills, UserJob tracking, deterministic explainable matching (`V3__jobs_and_matching.sql`).
- **Phase 4 — AI Job Analysis & Fit Scoring**: Provider-independent `AIProvider` SPI (`MockDeterministicAIProvider`, `OpenAIProvider`), versioned analyses (`V4__job_ai_analyses.sql`).
- **Phase 5 — Resume Management & Fit Analysis**: `Resume` aggregate root, structured sections (experiences, projects, skills, education, certifications), `JobResumeAnalysisService` (`V5__resume_management.sql`).
- **Phase 6 — Resume Tailoring, Versioning & Workflow**:
  - `TailoredResume` aggregate root and section suggestions (`V6__resume_tailoring.sql`).
  - Version sequencing per `(sourceResumeId, targetJobId)`.
  - Deterministic keyword coverage.
  - Section-level suggestions grounded in verified skills with anti-hallucination guard.
  - Human review lifecycle (`DRAFT` -> `UNDER_REVIEW` -> `APPROVED` / `REJECTED`).
  - Full Angular 18 frontend integration (`TailoredResumeWorkbenchComponent`).
  - Automated tests: 81 passing tests (0 failures, 0 errors).

---

## 3. Technology Stack & Ports
- **Backend:** Spring Boot 3.3.4 on Java 21 LTS (`http://localhost:8080`)
- **Database:** PostgreSQL 17 on `localhost:5432` (`job_command_center`), user `postgres`, default password `postgres`.
- **Frontend:** Angular 18 on `http://localhost:4200` (run with `npm.cmd start`).
- **Build Tools:** Maven for backend (`mvn test`), npm for frontend (`npm.cmd run build`).

---

## 4. Key Domain Entities & Flyway Migrations
- `V1__baseline.sql` — Flyway baseline.
- `V2__user_profile_skills.sql` — `users`, `profiles`, `skills`, `user_skills`.
- `V3__jobs_and_matching.sql` — `jobs`, `job_skills`, `user_jobs`.
- `V4__job_ai_analyses.sql` — `job_ai_analyses`, `job_ai_responsibilities`, `job_ai_technologies`, `job_ai_requirements`, `job_ai_red_flags`.
- `V5__resume_management.sql` — `resumes`, `resume_experiences`, `resume_projects`, `resume_skills`, `resume_education`, `resume_certifications`.
- `V6__resume_tailoring.sql` — `tailored_resumes`, `tailored_resume_suggestions`.
