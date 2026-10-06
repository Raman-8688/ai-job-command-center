# ADR-007: Repository Visibility & Data Privacy Posture

## Status
Accepted

## Context
The AI Job Command Center stores highly confidential information:
- The user's comprehensive career history, employment dates, and compensation targets.
- Proprietary recruiter email conversations, assessment links, and interview schedules.
- System prompt templates tailored to the user's personal career persona.
- OAuth client IDs and sensitive configuration parameters.
Publishing or managing this repository as a public open-source project creates severe data leakage risks.

## Decision
The Git repository must remain strictly **Private**. Furthermore:
- No personal resume text, real recruiter emails, or production tokens will be committed to version control.
- Test suites must use anonymized, synthetic fixture data only.
- Strict `.gitignore` rules prevent accidental commits of `.env` files or credentials.

## Alternatives Considered
- **Public Open-Source Repository:** Rejected due to the extreme danger of accidental personal data, recruiter identity, or token exposure.

## Consequences
- **Positive:** Complete privacy protection; prevents recruiter tracking; eliminates intellectual property leakage.
- **Negative:** None for a personal productivity command center.
