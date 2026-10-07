# Phase 1: Backend Foundation — Technical Architecture & Implementation

## 1. Objective
Establish the production-grade engineering foundation for the **AI Job Command Center** Modular Monolith backend in Java 21+ and Spring Boot 3.3.x. 

Phase 1 delivers the infrastructure plumbing required for all subsequent domain modules:
- PostgreSQL 16/17 connectivity and Flyway schema migration lifecycle.
- Production and local configuration profiles with zero hardcoded credentials.
- Spring Security stateless REST foundation with RFC 7807 entry points.
- Centralized REST error handling with uniform Problem Details contracts.
- Distributed request correlation (`X-Correlation-ID`) across MDC logging and HTTP headers.
- Spring Boot Actuator health checks and component observability.
- Comprehensive unit and integration test suites.

> [!IMPORTANT]
> In accordance with Phase 1 constraints, zero business domain entities (users, jobs, resumes, applications, emails, AI) were implemented.

---

## 2. Implemented Components & Package Structure

```text
com.jobcommandcenter
├── common
│   ├── correlation
│   │   ├── CorrelationIdFilter.java         # Intercepts/generates X-Correlation-ID & sets MDC
│   │   └── CorrelationIdHolder.java         # Static accessor for active request correlation ID
│   ├── error
│   │   ├── BusinessException.java           # Base 400 business exception
│   │   ├── ConflictException.java           # 409 resource conflict exception
│   │   ├── ErrorResponse.java               # RFC 7807 Problem Details response envelope
│   │   ├── GlobalExceptionHandler.java      # @RestControllerAdvice with uniform error mappings
│   │   ├── ResourceNotFoundException.java   # 404 resource not found exception
│   │   └── ValidationError.java             # Field-level validation detail record
│   └── logging
│       └── RequestLoggingFilter.java        # High-performance request latency & status logger
├── security
│   └── config
│       └── SecurityConfig.java              # Stateless SecurityFilterChain, CORS & RFC 7807 handlers
└── JobCommandCenterApplication.java         # Spring Boot entry point
```

---

## 3. Configuration & Profile Architecture

The configuration hierarchy separates environment-agnostic properties from runtime profiles:

- **`application.yml` (Base):** Configures server port, Flyway migration paths, JPA validation mode (`ddl-auto: validate`), Actuator endpoints (`health,info,metrics`), and correlation logging pattern.
- **`application-local.yml` (Profile: `local`):** Connects to local PostgreSQL instance with HikariCP connection pooling, enables verbose debugging, and provides environment variable bindings (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- **`application-test.yml` (Profile: `test`):** Configures in-memory H2 in PostgreSQL compatibility mode for hermetic, ultra-fast CI/CD test runs.

---

## 4. Database & Flyway Migration Setup

Flyway manages database evolution incrementally. 
- Migration files are stored in `src/main/resources/db/migration/`.
- **Baseline Migration:** `V1__baseline.sql` initializes database infrastructure tracking:
  ```sql
  CREATE TABLE IF NOT EXISTS system_metadata (
      metadata_key VARCHAR(100) PRIMARY KEY,
      metadata_value VARCHAR(255) NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
  );

  INSERT INTO system_metadata (metadata_key, metadata_value)
  VALUES ('schema_version', '1.0.0');

  INSERT INTO system_metadata (metadata_key, metadata_value)
  VALUES ('phase', '1_backend_foundation');
  ```
- Schema auto-generation (`hibernate.ddl-auto=create/update`) is strictly disabled; Hibernate operates in `validate` mode.

---

## 5. Security Foundation

- **Stateless Session Management:** REST APIs use `SessionCreationPolicy.STATELESS`.
- **CSRF:** Disabled for stateless REST endpoints with custom origin validation.
- **CORS:** Configured for `http://localhost:4200` with standard HTTP verbs and exposed `X-Correlation-ID` header.
- **Authorization Boundaries:**
  - Public: `/actuator/health/**`, `/actuator/info`, `/error`, `/api/public/**`.
  - Protected: All `/api/**` business routes require authentication.
- **RFC 7807 Authentication Gates:**
  - Unauthenticated requests trigger `AuthenticationEntryPoint` returning HTTP 401 with JSON Problem Details.
  - Unauthorized requests trigger `AccessDeniedHandler` returning HTTP 403 with JSON Problem Details.

---

## 6. Centralized Error Handling & Validation

All exceptions are intercepted by `GlobalExceptionHandler` (`@RestControllerAdvice`) and formatted according to RFC 7807 Problem Details:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more input fields failed validation constraints",
  "instance": "/api/v1/jobs",
  "timestamp": "2026-10-07T22:10:00Z",
  "correlationId": "8f9a2b1c-4e3d-4c8a-9f1e-2b3c4d5e6f7a",
  "validationErrors": [
    {
      "field": "title",
      "rejectedValue": "",
      "message": "Title is required"
    }
  ]
}
```

Handled HTTP Status Codes:
- `400 Bad Request`: Validation failures (`@Valid`), malformed JSON bodies, illegal arguments.
- `401 Unauthorized`: Missing or invalid authentication credentials.
- `403 Forbidden`: Authenticated identity lacking permissions.
- `404 Not Found`: Missing resources or unmatched endpoints.
- `405 Method Not Allowed`: Unsupported HTTP verb on valid routes.
- `409 Conflict`: Business state conflicts and database unique constraint violations.
- `500 Internal Server Error`: Unhandled server exceptions; internal stack traces and SQL details are sanitized and masked.

---

## 7. Request Correlation & Observability

1. **`CorrelationIdFilter`:** Intercepts every incoming HTTP request. If `X-Correlation-ID` is present and valid, it is preserved; otherwise, a random UUIDv4 is generated.
2. **MDC Injection:** Injects `correlationId` into SLF4J Mapped Diagnostic Context (MDC), ensuring all log statements include `[req:<id>]`.
3. **Response Header:** Echoes `X-Correlation-ID` back to the HTTP client for tracing across frontend and backend.
4. **`RequestLoggingFilter`:** Emits structured log entries (`HTTP GET /api/... -> 200 (15ms)`) without logging sensitive bodies or credentials.
5. **Spring Boot Actuator:** Exposes `/actuator/health` reporting status `UP` along with database connectivity health.

---

## 8. Testing Strategy & Results

The test suite includes 24 tests across unit and integration categories:
- **Unit Tests:**
  - `CorrelationIdFilterTest`: UUID generation, header propagation, sanitation.
  - `GlobalExceptionHandlerUnitTest`: Error response building, status mapping, stack trace masking.
- **Integration Tests:**
  - `SecurityIntegrationTest`: Public endpoints vs protected endpoints (401 check), mock user pass-through.
  - `ValidationAndErrorHandlingIntegrationTest`: 400 validation, malformed JSON, 404, 405, 409, 500 masking.
  - `DatabaseFlywayIntegrationTest`: In-memory H2 PostgreSQL mode, Flyway migration execution, `system_metadata` verification.
  - `PostgreSQLConnectionIntegrationTest`: Connects to live PostgreSQL instance (if available), verifies database metadata and Flyway table history.
  - `HealthEndpointIntegrationTest`: Actuator `/actuator/health` and component checks.
  - `JobCommandCenterApplicationTests`: Context boot test.

**Test Run Result:**
```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS (Total time: ~30s)
```

---

## 9. Local Development & Setup

1. **Start PostgreSQL:**
   ```bash
   # Option A: Start existing local PostgreSQL service
   # Option B: Run via Docker Compose
   docker-compose up -d postgres
   ```
2. **Run Backend with Local Profile:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. **Run Automated Test Suite:**
   ```bash
   cd backend
   mvn clean test
   ```
4. **Verify Health Endpoint:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

---

## 10. Known Limitations & Phase 2 Transition

- **Authentication Tokens:** Phase 1 implements stateless authorization boundaries and RFC 7807 entry points, but does not yet issue real JWTs. Real user authentication will be introduced alongside User Profile management.
- **Business Entities:** No business domain tables (users, jobs, resumes) exist in Phase 1.
- **Next Phase:** **Phase 2 — User Profile & Verified Skills**.
