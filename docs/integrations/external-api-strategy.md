# External API Integration Strategy

## 1. Principles
- **Circuit Breakers:** All external outbound calls (Gmail API, OpenAI API, Anthropic API) are wrapped with Resilience4j circuit breakers.
- **Rate Limit Compliance:** The application monitors response headers (e.g., `X-RateLimit-Remaining`, `Retry-After`) and throttles background tasks proactively.
- **Timeout Guarantees:** Default connection timeout: 5s; Read timeout: 30s.

## 2. Mocking & Local Development
In local testing environments or offline development, external clients switch seamlessly to deterministic mock adapters (`MockGmailClientAdapter`, `MockAIProvider`), allowing the complete UI and pipeline to be exercised without active internet access or API spend.
