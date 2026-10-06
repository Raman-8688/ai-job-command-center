# ADR-002: Relational Database Strategy — PostgreSQL

## Status
Accepted

## Context
The application domain requires complex relationships: users own verified skills; jobs link to companies and match analyses; applications track resumes, interview rounds, email threads, and immutable audit events. We need strict relational integrity, ACID transactions, robust JSON/JSONB indexing for AI metadata, full-text search capabilities, and battle-tested persistence.

## Decision
We select **PostgreSQL 16** as the primary relational database, managed via **Flyway** schema migrations.

## Alternatives Considered
- **MongoDB / NoSQL:** Rejected because core entities (applications, events, skills, jobs) are highly relational and require foreign key referential integrity.
- **SQLite / Embedded H2:** Considered for lightweight local storage, but rejected because PostgreSQL provides rich JSONB querying, robust concurrent processing, identical production parity, and advanced full-text indexing.

## Consequences
- **Positive:** Rock-solid relational integrity, native JSONB support for semi-structured AI payloads, mature Spring Data JPA and Flyway ecosystem.
- **Negative:** Requires running a PostgreSQL instance locally (easily satisfied via Docker Compose).
