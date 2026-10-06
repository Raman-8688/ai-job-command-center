# Database Design & Schemas

## 1. Principles
- **Relational Integrity:** Foreign keys enforce strict relational consistency across users, jobs, resumes, and applications.
- **Audit Immutability:** Event tables (e.g., `application_events`, `audit_logs`) are append-only.
- **UUID Primary Keys:** All tables use UUIDv4 (`gen_random_uuid()`) primary keys to prevent enumeration attacks and simplify data migration.
- **Timestamps:** Every mutable table includes `created_at` and `updated_at` (UTC `TIMESTAMPTZ`).

## 2. Table Summary

### 2.1 Identity & Skills
- `users`: Core credentials and state.
- `user_profiles`: Contact details, links, targets.
- `skills`: Master directory of skills with categories.
- `user_skills`: Candidate verified skills with experience types and verification flags.

### 2.2 Jobs & Ingestion
- `companies`: Company records, domain, industry.
- `jobs`: Raw job posting, normalized fields, deduplication hash, current status.
- `job_analyses`: AI breakdown, match scores (0-100), missing skills JSON.

### 2.3 Resumes & Applications
- `resumes`: Master resume definitions.
- `resume_versions`: Tailored resume snapshots tied to jobs.
- `applications`: Application lifecycle instances linking jobs, resumes, and status.
- `application_events`: Immutable status change journal.
- `recruiters`: Recruiter directory and correspondence tracking.

### 2.4 Email & Intelligence
- `emails`: Stored email metadata and thread IDs.
- `email_classifications`: AI categories, confidence, extracted dates/deadlines.
- `interviews`: Scheduled rounds, meeting links, status.
- `oas`: Online assessment platforms, deadlines, status.

### 2.5 AI & Operations
- `ai_prompts`: Versioned prompt templates.
- `ai_requests`: Complete audit of prompt invocations, token usage, and latency.
- `approval_queue`: Pending Level 2/3 human actions.
- `audit_logs`: System security audit log.
