# Software Requirements Specification (SRS)

## 1. System Context & Constraints
The system operates as a single deployable server application (Java 21 / Spring Boot) serving an Angular SPA.
- **Operating System Parity:** Compatible with Windows 11, macOS, and Linux.
- **Storage:** PostgreSQL 16 database and local directory for file persistence.
- **Network Boundaries:** Requires outbound HTTPS connectivity to Google APIs (Gmail) and configured AI Provider endpoints (OpenAI / Anthropic / Gemini). If configured for local AI (Ollama), runs fully offline with respect to AI inference.

## 2. System Actors
- **Primary User:** The authenticated job seeker interacting with the Angular UI.
- **AI Engine (Internal Actor):** Background analysis and generation services executing bounded inference tasks.
- **Gmail Worker (Internal Actor):** Scheduled task polling Gmail API for incoming job messages.

## 3. High-Level System Features
- User Profile and Verified Skill Matrix Management.
- Job Description Ingestion, Normalization, Deduplication, and Fit Scoring.
- Master Resume Storage, Job-Specific Tailoring, and PDF Generation.
- Bi-directional Gmail Integration and Automated Email Classification.
- Application Lifecycle State Tracking with Event Sourcing semantics.
- Human Approval Queue for all Level 2 and Level 3 actions.
- Interview & OA Preparation Briefing Generator.
- Pipeline Funnel and Skill Gap Analytics.
