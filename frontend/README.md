# Frontend - AI Job Command Center

## Architecture Overview
The frontend is a single-page application (SPA) built with **Angular (TypeScript)**. It provides a dense, real-time dashboard and command center for managing the job hunt workflow.

### Planned Key Views
- **Executive Command Center:** Daily KPIs, action items, pending approvals, interview calendar, and active OAs.
- **Job Discovery & Pipeline:** Kanban and tabular views of discovered, shortlisted, applied, and active opportunities.
- **AI Job Analyzer:** Side-by-side view of job descriptions vs. profile fit, gap analysis, and match breakdown.
- **Resume Studio:** Master resume manager, dynamic version diffing, and PDF preview.
- **Human Approval Queue:** High-priority review screen for Level 2 & 3 actions (recruiter messages, tailored resumes, follow-ups).
- **Communication Hub:** Synchronized job-related email threads, detected stage changes, and AI-drafted replies.
- **Interview & OA Prep:** Structured interview questions, behavioral outlines, technical cheat-sheets, and countdown timers.
- **Search Analytics:** Funnel conversion graphs (Applied -> OA -> Interview -> Offer) and skill gap analytics.

## Design Standards
- Clean, high-density professional UI (Angular Material / clean UI component library).
- Responsive layout prioritizing desktop productivity workflows.
- Strictly typed REST API models matching backend OpenAPI DTO definitions.
