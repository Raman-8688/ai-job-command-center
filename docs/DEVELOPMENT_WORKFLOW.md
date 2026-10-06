# Engineering Rules & Development Workflow

This document sets the mandatory engineering discipline for developing the **AI Job Command Center**. Every developer and automated agent must adhere to this lifecycle.

---

## 1. The Core Implementation Cycle

No feature is written directly without prior design and documentation. Every increment follows this strict progression:

```
[Requirement Review]
        |
        v
    [Design & Architecture Validation]
        |
        v
    [Documentation Update in docs/]
        |
        v
    [Smallest Testable Implementation]
        |
        v
    [Unit Test Execution & Coverage]
        |
        v
    [Integration Test Execution]
        |
        v
    [Peer/Self Architecture Review]
        |
        v
    [Documentation & Changelog Sync]
        |
        v
    [Clean Commit & Pull Request]
```

---

## 2. Step-by-Step Engineering Rules

### Step 1: Read Project Master Plan
Before starting any phase, review [docs/PROJECT_MASTER_PLAN.md](PROJECT_MASTER_PLAN.md) and related architectural documents. Understand the module boundaries and constraints.

### Step 2: Check Existing Implementation
Never assume a service or entity doesn't exist. Inspect existing packages under `backend/` and `frontend/` to avoid duplicate utilities, parallel models, or diverging interfaces.

### Step 3: Identify Dependencies & Avoid Leakage
Ensure that any new functionality lives in the correct module. Module-to-module dependencies must go through published service contracts or domain events, never direct repository queries into another module's internal tables.

### Step 4: Update Documentation First
Document any new data structures, endpoints, or rules in `docs/` before or alongside writing code. If a technical requirement is ambiguous, make a safe choice, record it in `docs/decisions/`, and request user confirmation.

### Step 5: Implement the Smallest Production Increment
Do not attempt large speculative refactors or monolithic multi-feature commits. Build the minimal coherent component with full production-grade rigor (error handling, validation, types).

### Step 6: Write Tests (Mandatory)
Every business method must have unit tests.
- Core algorithms (scoring, deduplication, state transitions) must have 100% boundary test coverage.
- AI parsers must test malformed JSON, missing fields, and hallucinated payloads.
- Mocks are used for external APIs (Gmail, OpenAI), but internal domain tests should run quickly in memory.

### Step 7: Run Local Verifications
Verify that the build passes cleanly:
```bash
# Backend verification
cd backend && ./mvnw clean test

# Frontend verification
cd frontend && npm test -- --watch=false
```

### Step 8: Anti-Hallucination & Truthfulness Verification
Whenever modifying the AI or Resume modules, verify:
- Does the prompt require grounding in the user's verified profile?
- Is there a validator intercepting the AI output before it touches persistence?
- Is there any code path that could automatically submit an application without Level 3 human approval? (Such paths are strictly prohibited).

### Step 9: Summarize & Report
Provide a structured status report detailing:
1. Files created / modified.
2. Architectural decisions made.
3. Tests executed and passing.
4. Next logical increment.

---

## 3. Git Commit Conventions

All commits must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
- `feat(module): add job ingestion parser`
- `fix(application): prevent duplicate status transitions`
- `test(ai): add JSON repair regression test`
- `docs(api): document job scoring endpoints`
- `refactor(resume): streamline section rendering`
- `chore(deps): update testcontainers version`

Do **NOT** commit:
- Unused commented-out code.
- Temporary scratch files.
- `.env` files or credentials.
