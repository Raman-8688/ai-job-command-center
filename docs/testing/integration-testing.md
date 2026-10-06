# Integration Testing Standards

## 1. Scope & Tooling
- **Frameworks:** `@SpringBootTest`, `@AutoConfigureMockMvc`, Testcontainers (PostgreSQL).
- **Parity Guarantee:** Tests run against real PostgreSQL instances via Testcontainers, verifying Flyway migration integrity and native JSONB queries.

## 2. Target Scenarios
1. **Repository Queries:** Complex JPQL and native queries across jobs, applications, and events.
2. **REST Controllers:** MockMvc tests verifying request validation annotations (`@Valid`), HTTP status codes, and RFC 7807 error envelopes.
3. **Transaction Boundaries:** Verifies rollback behavior upon exceptions and ensures event listeners execute at `@TransactionalEventListener` phase.
