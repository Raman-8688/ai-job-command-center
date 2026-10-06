# ADR-004: Human-in-the-Loop 3-Tier Automation Policy

## Status
Accepted

## Context
Automated job search tools often suffer from catastrophic failure modes: spamming irrelevant job boards, emailing recruiters unvetted nonsense, or fabricating resume experience. Because this application is for real-world personal use, accidental automated external actions could permanently damage professional reputation. Conversely, requiring manual approval for every internal database update eliminates the efficiency benefits of automation.

## Decision
We enforce a rigid **Three-Tier Automation Policy**:
1. **Level 1 (Fully Automatic):** Safe internal operations only (reading/classifying emails, parsing JDs, computing match scores, indexing, reminder notifications).
2. **Level 2 (AI-Generated Draft):** Artifact generation that impacts the user's career presentation (tailored resumes, cover letters, recruiter replies). Artifacts are parked in a **Human Approval Queue** awaiting explicit user review, editing, or approval.
3. **Level 3 (External Action):** External mutations (sending emails through Gmail API, submitting applications). Strictly initiated by explicit human action. Blind auto-submission is architecturally forbidden.

## Alternatives Considered
- **Full Autonomous Agent:** Allowing the AI to autonomously apply and email. Rejected as extremely unsafe and reputationally catastrophic.
- **Pure Manual Tool:** No AI draft assistance. Rejected as it fails to reduce repetitive candidate workload.

## Consequences
- **Positive:** Guarantees zero unapproved external communications; eliminates hallucinations reaching recruiters; maintains user control while drastically reducing draft creation time.
- **Negative:** Requires an explicit Human Approval Queue UI and state tracking.
