# AI Engine Architecture

## 1. Core Principles
The AI module is decoupled from external model APIs and operates strictly through bounded, idempotent inference contracts.

```
+--------------------------------------------------------------------------+
|                            AI ENGINE (Java 21)                           |
|                                                                          |
|  +--------------------+   +---------------------+   +-----------------+  |
|  |   PromptManager    |   | ContextAssembler    |   | JSONValidator   |  |
|  | - Versioned Prompts|   | - Verified Profile  |   | - Schema Check  |  |
|  | - Template Engine  |   | - Grounding Facts   |   | - Factual Audit |  |
|  +---------+----------+   +----------+----------+   +--------^--------+  |
|            |                         |                       |           |
|            +------------+------------+                       |           |
|                         v                                    |           |
|             +-----------------------+                        |           |
|             |       AIService       |                        |           |
|             |  (Retry, Cost Logger) |------------------------+           |
|             +-----------+-----------+                                    |
+-------------------------|------------------------------------------------+
                          v
         +---------------------------------+
         |       AIProvider Interface      |
         +----------------+----------------+
                          |
      +-------------------+-------------------+
      |                   |                   |
      v                   v                   v
+------------+      +------------+      +------------+
|  OpenAI    |      | Anthropic  |      |   Ollama   |
|  Provider  |      |  Provider  |      |  (Local)   |
+------------+      +------------+      +------------+
```

## 2. Responsibilities
- **Context Assembler:** Gathers required domain entities (verified user skills, relevant past accomplishments, JD text) and injects anti-hallucination constraints into the system prompt.
- **Prompt Manager:** Manages immutable, version-controlled prompt templates.
- **AI Provider Router:** Executes LLM requests with exponential backoff and timeout safeguards.
- **Output Validator:** Parses JSON, validates syntax, verifies types via Bean Validation, and runs factual consistency checks.
- **Cost & Token Tracker:** Records prompt tokens, completion tokens, latency, and estimated cost in `ai_requests`.
