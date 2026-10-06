# Hallucination Prevention & Anti-Fabrication Architecture

## 1. The Core Threat
Generative AI models excel at flattering phrasing, but routinely "help" candidates by inventing technical proficiencies, claiming production Kubernetes expertise when the user only knows Docker, or fabricating years of experience. In real job applications, getting caught in an interview with fabricated resume claims permanently damages career reputation.

## 2. Architectural Guardrails

### 2.1 Closed-World Constraint in Prompts
System prompts strictly command the LLM:
> "You are restricted to the facts provided in [VERIFIED_EXPERIENCE] and [VERIFIED_SKILLS]. You must NEVER infer, extrapolate, or invent technologies, companies, dates, metrics, or responsibilities. If a required job skill is missing from the candidate's verified profile, you MUST mark it as missing."

### 2.2 Algorithmic Grounding Interceptor (Code-Level)
We do not rely solely on prompt adherence. Java code executes an automated fact-check:
1. Extract all technical tokens and keywords from the generated resume bullet points.
2. Cross-reference each extracted keyword against the candidate's `user_skills` database table.
3. If any unverified technology is detected:
   - Flag the bullet point with an inline warning badge in the UI: `⚠️ Unverified Skill Detected`.
   - Prevent automatic approval until the candidate either verifies the skill or removes the bullet.
