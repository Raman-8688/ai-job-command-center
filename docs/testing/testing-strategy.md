# Testing Strategy & Test Pyramid

## 1. Testing Philosophy
Testing is not an afterthought; it is mandatory for every phase of development. Because this application manages real career data and interacts with live email inboxes, defects in state transitions or AI prompts can directly compromise a job search.

```
          / \
         /   \       End-to-End Tests (Cypress / Playwright)
        / E2E \      [Critical user journeys: Ingest -> Tailor -> Approve]
       /-------\
      /         \    Integration Tests (Spring Boot Test / Testcontainers)
     /  INTEG    \   [PostgreSQL JPA tests, Flyway migrations, API contracts]
    /-------------\
   /               \  Unit & AI Boundary Tests (JUnit 5, Mockito, AssertJ)
  /      UNIT       \ [Scoring formulas, Deduplication, Factual Grounding, Schemas]
 /-------------------\
```

## 2. Quality Gates
- **Unit Test Line Coverage:** Minimum 85% on domain logic and state transitions.
- **AI Factual Regression:** 100% pass rate on anti-hallucination test suites.
- **Continuous Integration:** CI pipeline must pass cleanly before any pull request merge.
