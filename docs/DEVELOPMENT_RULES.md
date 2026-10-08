# Development Rules & Anti-Hallucination Guidelines

## 1. Truthfulness & Anti-Hallucination Rules
- **No Fabricated Data**: Never invent skills, employers, project accomplishments, metrics, or education.
- **Strict Skill Grounding**: Only skills marked `isVerified() == true` in `user_skills` are treated as possessed by the candidate.
- **Missing Skills Handling**: If a job requires a technology not present in verified skills, mark it as `[NOT_ENOUGH_EVIDENCE]`. Do **not** invent experience to fill the gap.
- **Traceable Evidence**: Every tailoring recommendation must reference verifiable source records (experience, project, verified skill) or state that evidence is missing.

---

## 2. Architectural Boundaries
- **Modular Monolith**: Keep all modules inside the single deployment unit. No microservices.
- **Hexagonal Layering**: Controllers map to DTOs; Application services orchestrate domain aggregates; Repositories define interfaces in `domain` and adapters in `infrastructure`. Never leak JPA entities into API responses.
- **Master Resume Immutability**: Master resumes in `resumes` table must never be automatically overwritten. Tailored resumes are distinct child drafts with explicit version numbers.

---

## 3. Human Control & Workflows
- **Approval Queue**: External interactions (e.g. applications, email drafts, outbound communications) and finalized tailored resumes require explicit human review and transition to `APPROVED`.

---

## 4. Git & Database Safety
- **No Git Commits by AI**: Git operations (`add`, `commit`, `push`, `merge`, `pr`) are reserved strictly for the human developer.
- **No Destructive Database Commands**: Never drop database, drop tables, truncate data, or modify existing Flyway migration scripts. Schema evolution is purely forward-migrating via Flyway.
