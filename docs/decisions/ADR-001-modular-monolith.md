# ADR-001: Architectural Pattern — Modular Monolith

## Status
Accepted

## Context
The AI Job Command Center is a personal, high-discipline platform designed to manage an individual software engineer's job hunt. While it encompasses multiple domains (user profile, job ingestion, resume generation, email processing, interview preparation, automation, analytics), it is deployed for single-user or small personal workloads. Introducing a microservices architecture (Eureka, API Gateway, Kafka, independent network services, distributed transactions) would incur immense operational overhead, complex debugging, distributed state failure modes, and resource inefficiency without providing any horizontal scaling benefit.

## Decision
We choose a **Modular Monolith** architecture built on Java 21+ and Spring Boot. All domain modules will live within a single deployable artifact and repository, segregated by clear Java package boundaries, explicit module contracts, and isolated database entities.

## Alternatives Considered
- **Microservices:** Rejected due to excessive operational complexity, high local RAM consumption, network latency, and unnecessary distributed infrastructure.
- **Unstructured Monolith ("Big Ball of Mud"):** Rejected due to lack of domain boundaries, making future maintenance and refactoring difficult.

## Consequences
- **Positive:** Simple local development, single JVM process, immediate transactional consistency, shared in-memory event bus, straightforward testing.
- **Negative:** Requires strong discipline to avoid illegal cross-module internal calls and entity leaks.
