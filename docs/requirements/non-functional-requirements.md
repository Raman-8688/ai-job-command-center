# Non-Functional Requirements Specification

Each requirement is uniquely identified by `NFR-XXX`.

---

## 1. Performance & Latency
- **NFR-001:** Standard REST API endpoints (retrieving jobs, applications, dashboard metrics) shall respond within 200 milliseconds under single-user load.
- **NFR-002:** AI job analysis and resume tailoring operations shall execute asynchronously; the frontend shall receive immediate task acknowledgment (<100ms) and poll or listen for completion.
- **NFR-003:** Local PDF generation for tailored resumes shall complete within 3 seconds.

---

## 2. Reliability & Idempotency
- **NFR-004:** Email synchronization and processing must be strictly idempotent. Ingesting the same Gmail message ID multiple times shall result in zero duplicate application events or duplicate database rows.
- **NFR-005:** Scheduled tasks must gracefully recover from network timeouts, external API rate limits, or transient provider outages using exponential backoff without crashing the application.

---

## 3. Security & Privacy
- **NFR-006:** Zero credentials, API keys, or OAuth client secrets shall be stored in plaintext in the codebase, database, or version control.
- **NFR-007:** OAuth refresh tokens and sensitive API keys must be encrypted at rest using AES-256-GCM.
- **NFR-008:** Application logs must never contain raw passwords, OAuth access/refresh tokens, or unredacted confidential email bodies.
- **NFR-009:** The application must enforce strict CORS policies allowing only the designated frontend client origin (e.g., `http://localhost:4200`).

---

## 4. Maintainability & Code Quality
- **NFR-010:** The codebase must maintain strict Modular Monolith encapsulation: modules interact via defined service interfaces, not direct cross-module entity relationships or ad-hoc table joins.
- **NFR-011:** Unit test coverage on core scoring, state machine transitions, deduplication algorithms, and factual grounding checks must exceed 85%.

---

## 5. Portability & Operational Simplicity
- **NFR-012:** The complete system must run locally using standard OpenJDK 21, Node.js 20+, and a single PostgreSQL 16 container launched via `docker-compose.yml`.
