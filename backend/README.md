# Backend — AI Job Command Center

The backend is a high-discipline **Modular Monolith** built on **Java 21+** and **Spring Boot 3.3.4**, designed for production-grade personal job search workflows.

---

## 1. Prerequisites
- **Java:** OpenJDK 21 LTS or Oracle JDK 21+
- **Build Tool:** Apache Maven 3.9+ (or included `./mvnw`)
- **Database:** PostgreSQL 16+ (or local container via Docker)
- **Environment:** Windows, macOS, or Linux

---

## 2. Technology Stack & Key Dependencies
- **Runtime:** Java 21, Spring Boot 3.3.4
- **Web & Validation:** Spring Web, Jakarta Bean Validation (Hibernate Validator)
- **Persistence & Migrations:** Spring Data JPA, PostgreSQL Driver, Flyway Community Edition
- **Security:** Spring Security 6 (Stateless REST, RFC 7807 entry points, BCrypt)
- **Observability:** Spring Boot Actuator, SLF4J with MDC Request Correlation (`X-Correlation-ID`)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Security Test, H2 (for isolated test profiles)

---

## 3. Environment Variables
The application reads configuration from environment variables with safe development defaults:

| Variable | Default (Local) | Purpose |
|---|---|---|
| `APP_PORT` | `8080` | Server HTTP port |
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile (`local`, `test`, `prod`) |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `job_command_center` | PostgreSQL database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `DB_URL` | Auto-derived from host/port/db | Full JDBC connection URL |
| `APP_JWT_SECRET` | 64-char development key | HMAC-SHA512 signing key for JWT tokens |
| `APP_JWT_EXPIRATION_MS` | `86400000` (24h) | JWT token expiration time in milliseconds |

---

## 4. Database Setup & Migrations
Database migrations are strictly version-controlled with **Flyway** in `src/main/resources/db/migration/`.

1. Ensure PostgreSQL is running on port 5432.
2. Create the local database if not already present:
   ```sql
   CREATE DATABASE job_command_center;
   ```
3. When the Spring Boot application boots with the `local` profile, Flyway automatically validates and applies all pending migrations:
   - `V1__baseline.sql`: Initializes `system_metadata`.
   - `V2__user_profile_skills.sql`: Initializes `users`, `profiles`, `profile_target_roles`, `profile_preferred_locations`, `skills`, and `user_skills`.
   - `V3__jobs_and_matching.sql`: Initializes `jobs`, `job_skills`, and `user_jobs`.
   - `V4__job_ai_analyses.sql`: Initializes `job_ai_analyses`, `job_ai_responsibilities`, `job_ai_technologies`, `job_ai_requirements`, and `job_ai_red_flags`.
   - `V5__resume_management.sql`: Initializes `resumes`, `resume_experiences`, `resume_projects`, `resume_skills`, `resume_education`, and `resume_certifications`.
4. Schema auto-creation (`ddl-auto=create/update`) is permanently disabled; Hibernate runs with `ddl-auto: validate`.

---

## 5. API Endpoints

### Authentication & Users
- `POST /api/auth/login`: Authenticate email/password and obtain JWT Bearer token
- `GET /api/users/me`: Current user identity details (Bearer authenticated)

### Candidate Profile
- `GET /api/profile`: Retrieve candidate profile (auto-initializes on first call)
- `PUT /api/profile`: Update candidate professional profile

### Skill Catalog & Candidate Skills
- `GET /api/skills`: Search catalog skills (`?query=...&category=...`)
- `GET /api/skills/{id}`: Retrieve catalog skill by ID
- `POST /api/skills`: Register new standardized skill in catalog
- `GET /api/profile/skills`: List candidate's claimed skills
- `POST /api/profile/skills`: Add skill claim (Anti-hallucination: AI suggestions remain unverified)
- `PUT /api/profile/skills/{id}`: Update skill claim / explicit verification
- `DELETE /api/profile/skills/{id}`: Remove skill claim

### Job Discovery, Management & Matching (Phase 3)
- `POST /api/jobs`: Ingest canonical job posting with required/preferred skills (deduplication enforced)
- `GET /api/jobs`: Search and filter jobs (`query`, `status`, `source`, `workMode`, `company`, `location`, `page`, `size`)
- `GET /api/jobs/{id}`: Retrieve canonical job details by ID with resolved skill names
- `PUT /api/jobs/{id}`: Update job details, metadata, and skills
- `DELETE /api/jobs/{id}`: Delete job posting
- `GET /api/jobs/{id}/match`: Calculate deterministic 6-dimension match score against candidate verified profile
- `GET /api/jobs/matches`: Calculate and list matches for all active jobs sorted by overall score
- `PUT /api/jobs/{id}/user-status`: Update candidate interaction status (`DISCOVERED`, `SAVED`, `SHORTLISTED`, `IGNORED`)

### AI Job Analysis & Fit Scoring (Phase 4)
- `POST /api/jobs/{id}/ai-analysis`: Trigger AI analysis for a job posting (increments version)
- `GET /api/jobs/{id}/ai-analysis`: Retrieve the latest completed AI analysis for a job
- `GET /api/jobs/{id}/ai-analysis/history`: Retrieve full historical AI analysis runs for a job
- `GET /api/jobs/{id}/ai-fit`: Calculate explainable fit evaluation layering AI gap analysis onto deterministic score

### Resume Management & Analysis (Phase 5)
- `POST /api/resumes`: Create new candidate resume in `DRAFT` status
- `GET /api/resumes`: List all resumes belonging to authenticated candidate
- `GET /api/resumes/{id}`: Retrieve complete resume with all structured sections
- `PUT /api/resumes/{id}`: Update resume metadata and structured sections
- `DELETE /api/resumes/{id}`: Delete resume
- `POST /api/resumes/{id}/activate`: Activate resume
- `POST /api/resumes/{id}/archive`: Archive resume
- `GET /api/resumes/{resumeId}/jobs/{jobId}/analysis`: Evaluate resume fit against a job posting
- `POST /api/resumes/{resumeId}/jobs/{jobId}/analysis`: Trigger/re-evaluate resume fit against job posting

---

## 6. Running the Backend Locally

```bash
cd backend

# Option A: Run via Maven
mvn spring-boot:run

# Option B: Run with an explicit profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Once started:
- Base API URL: `http://localhost:8080`
- Actuator Health: `http://localhost:8080/actuator/health`
- Actuator Info: `http://localhost:8080/actuator/info`

---

## 7. Running Tests

The test suite runs hermetically and does not require active external services:

```bash
cd backend
mvn clean test
```

The test profile (`test`) uses an in-memory database with PostgreSQL dialect emulation and validates:
- Flyway migrations (V1 and V2)
- Actuator health probes and component status
- Request correlation generation and header propagation
- Centralized exception handling and RFC 7807 Problem Details
- User authentication, JWT issuance, and principal isolation
- Profile CRUD and child collection persistence
- Skill catalog normalization and anti-hallucination verification enforcement

If a local PostgreSQL instance is running on port 5432, `PostgreSQLConnectionIntegrationTest` will also automatically verify connectivity and migrations against live PostgreSQL.

---

## 8. Observability & Logging
Every incoming request receives an `X-Correlation-ID`:
- Injected into SLF4J MDC (`[req:<id>]`).
- Returned to the client in HTTP response headers.
- Included in RFC 7807 error responses for instant log triage.
- Log pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [req:%X{correlationId:-none}] - %msg%n`
---

## 8. Troubleshooting

### Problem: `Connection refused: connect` to PostgreSQL
- **Resolution:** Verify that PostgreSQL is running (`docker-compose up -d postgres` or check your local PostgreSQL service). Verify port 5432 is accessible.

### Problem: `Migration checksum mismatch`
- **Resolution:** Never edit an already-applied migration file. In local development, you can clean the local database schema or write an incremental migration `V2__...sql`.

### Problem: `401 Unauthorized` on `/api/...`
- **Resolution:** All endpoints under `/api/**` (except `/api/public/**`) require authentication by design. In Phase 1, use `@WithMockUser` in automated tests.
