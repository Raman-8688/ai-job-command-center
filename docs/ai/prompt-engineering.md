# Prompt Engineering Guidelines

## 1. Principles
1. **Explicit Role & Task Definition:** Clearly define the AI's persona as an objective executive recruiter or factual career strategist.
2. **Strict Negative Constraints (Guardrails):** Explicitly list what the model MUST NOT do (e.g., "Do not invent technologies", "Do not assume experience not present in Verified Skills").
3. **Structured Output Enforcement:** Demand raw JSON adhering strictly to a JSON schema. Avoid conversational fluff or markdown code block prefixes where supported by API parameters (`response_format: json_object`).
4. **Few-Shot Grounding:** Include canonical input/output examples for tricky parsing tasks (e.g., detecting interview dates from vague email wording).

## 2. Standard Prompt Architecture
Every prompt template consists of:
- **System Prompt:** Core persona, security guardrails, anti-hallucination rules.
- **Context Injection:** Candidate's verified skills, verified past achievements, target JD.
- **Task Instruction:** Specific extraction or synthesis instructions.
- **JSON Schema:** Exact output contract required.
