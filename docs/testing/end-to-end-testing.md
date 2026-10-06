# End-to-End Testing Specification

## 1. Scope
E2E tests validate complete user journeys across the Angular UI and Spring Boot backend against a seeded test environment.

## 2. Key Golden Paths
1. **Journey 1: Ingestion to Approved Application**
   - User inputs JD -> Match score displays -> Tailor resume clicked -> Diff inspected in Human Approval Queue -> Approved -> Status verified as `READY_FOR_APPLICATION`.
2. **Journey 2: Email Receipt to Stage Progression**
   - Simulated interview email received -> Classification verified as `INTERVIEW` -> Status changes on pipeline board -> Study dossier generated.
