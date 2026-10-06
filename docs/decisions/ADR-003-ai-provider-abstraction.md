# ADR-003: AI Provider Decoupling & Abstraction

## Status
Accepted

## Context
AI model APIs evolve rapidly. Cloud providers (OpenAI, Anthropic, Google Gemini) frequently change pricing, context window limits, and SDK conventions. Furthermore, for personal privacy or offline development, running a local model (via Ollama or vLLM) is highly desirable. Coupling core business services (job scoring, resume tailoring, email classification) directly to a specific cloud vendor SDK creates vendor lock-in and brittle code.

## Decision
We implement a vendor-neutral interface abstraction (`AIProvider`) in Java:
```java
public interface AIProvider {
    AIResponse generate(AIRequest request);
    boolean isAvailable();
    String getProviderName();
}
```
All business modules consume `AIService`, which routes requests through configured `AIProvider` beans based on environment configuration (`AI_PROVIDER=OPENAI`, `AI_PROVIDER=OLLAMA`, etc.).

## Alternatives Considered
- **Direct OpenAI SDK coupling:** Quickest to write, but prevents swapping to local models or alternative providers.
- **Spring AI directly everywhere:** Spring AI can be evaluated as an underlying adapter, but our domain layer must still maintain its own stable internal request/response contracts and prompt versioning.

## Consequences
- **Positive:** Seamless switching between cloud and local LLMs; simplified unit testing via clean mocks; unified token and cost logging.
- **Negative:** Requires maintaining our own clean abstraction contracts and DTO mappings.
