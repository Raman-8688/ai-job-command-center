# AI Provider Strategy

## 1. Provider Contract
All LLM integrations implement the unified `AIProvider` contract:
```java
public interface AIProvider {
    AIResponse generate(AIRequest request);
    boolean isAvailable();
    String getProviderName();
}
```

## 2. Supported Implementations
1. **Cloud Providers:**
   - `OpenAIProvider` (Default model: `gpt-4o-mini` for high speed & cost efficiency; `gpt-4o` for complex resume synthesis).
   - `AnthropicProvider` (`claude-3-5-sonnet` for nuanced email drafting).
   - `GeminiProvider` (`gemini-1.5-pro` / `flash` for massive context window analysis).
2. **Local Privacy-First Providers:**
   - `OllamaProvider` (`llama3.2`, `mistral`, `deepseek-r1` running locally at `http://localhost:11434`). Provides complete data privacy and offline functionality.

## 3. Fallback & Circuit Breaking
If the primary configured provider fails due to rate limits or API downtime, `AIService` can fall back to a secondary provider or preserve the raw input in an `ANALYSIS_FAILED` state without corrupting user data.
