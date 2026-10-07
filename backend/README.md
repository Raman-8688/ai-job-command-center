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

---

## 4. Database Setup & Migrations
Database migrations are strictly version-controlled with **Flyway** in `src/main/resources/db/migration/`.

1. Ensure PostgreSQL is running on port 5432.
2. Create the local database if not already present:
   ```sql
   CREATE DATABASE job_command_center;
   ```
3. When the Spring Boot application boots with the `local` profile, Flyway automatically validates and applies all pending migrations (e.g., `V1__baseline.sql`).
4. Schema auto-creation (`ddl-auto=create/update`) is permanently disabled; Hibernate runs with `ddl-auto: validate`.

---

## 5. Running the Backend Locally

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

## 6. Running Tests

The test suite runs hermetically and does not require active external services:

```bash
cd backend
mvn clean test
```

The test profile (`test`) uses an in-memory database with PostgreSQL dialect emulation and validates:
- Flyway migration application
- Actuator health probes and component status
- Request correlation generation and header propagation
- Centralized exception handling and RFC 7807 Problem Details
- Security authorization rules (401 on protected endpoints, 200 on public)

If a local PostgreSQL instance is running on port 5432, `PostgreSQLConnectionIntegrationTest` will also automatically verify connectivity against live PostgreSQL.

---

## 7. Observability & Logging
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
