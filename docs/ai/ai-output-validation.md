# AI Output Validation Framework

## 1. Zero Trust for AI Output
Raw text returned by an LLM is treated as untrusted external user input. It must pass three layers of defense before entering the domain layer:

```
[ Raw LLM Output ]
        |
        v
 [ Layer 1: JSON Syntactic Validator & Auto-Sanitizer ]
 (Strips markdown code fences, fixes trailing commas)
        |
        v
 [ Layer 2: Strongly Typed DTO Deserialization & Bean Validation ]
 (Enforces @NotNull, @Min, @Max, regex formats via Jackson)
        |
        v
 [ Layer 3: Domain Factual Grounding Validator ]
 (Verifies claimed skills against candidate's verified profile)
        |
        v
[ Persisted Entity / Approval Queue ]
```

## 2. Low-Confidence Escalation
For classification tasks (e.g., email stage detection):
- If `confidenceScore < 0.80`, the system automatically assigns the category `MANUAL_REVIEW`.
- No state transition is automatically triggered until the user reviews the message.
