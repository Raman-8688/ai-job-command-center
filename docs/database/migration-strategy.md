# Database Migration Strategy

## 1. Migration Framework: Flyway
Database schema evolution is strictly managed via **Flyway Community Edition**. 
- Production or development schemas must **never** be modified manually or via Hibernate's `ddl-auto=update`.
- Spring Boot configuration enforces `spring.jpa.hibernate.ddl-auto=validate`.

## 2. Naming Conventions & Structure
Migration scripts reside in `backend/src/main/resources/db/migration/` and follow Flyway versioned script standards:
`V<MAJOR>_<MINOR>__<description>.sql`

### Planned Initial Migration Sequence:
- `V1_0__init_users_and_skills.sql`: Core users, user_profiles, skills, user_skills.
- `V1_1__init_jobs_and_companies.sql`: Companies, jobs, job_analyses.
- `V1_2__init_resumes.sql`: Resumes, resume_sections, resume_versions.
- `V1_3__init_applications_and_events.sql`: Applications, application_events, recruiters.
- `V1_4__init_emails_and_intelligence.sql`: Emails, email_classifications, interviews, oas.
- `V1_5__init_automation_and_audit.sql`: Approval_queue, ai_requests, audit_logs.

## 3. Rollback & Forward Migration Policy
- In PostgreSQL production environments, migrations must be non-destructive (e.g., adding nullable columns, creating indexes concurrently).
- Irreversible changes (dropping columns) are phased across multiple releases.
