# Integration Architecture

## 1. External System Interfaces
The system communicates with external services across two primary boundaries:
1. **Google Gmail REST API:** For email synchronization, draft creation, and sending.
2. **AI Provider APIs (Cloud or Local):** For natural language understanding, text extraction, and generation.

```
+--------------------------------------------------------------------------+
|                       SPRING BOOT MONOLITH RUNTIME                       |
|                                                                          |
|   +-----------------------+              +---------------------------+   |
|   |  GmailSyncService     |              |     AIService             |   |
|   |  (Scheduled & Polled) |              |     (Async LLM Gateway)   |   |
|   +-----------+-----------+              +-------------+-------------+   |
|               |                                        |                 |
|   +-----------v-----------+              +-------------v-------------+   |
|   |  GmailClientAdapter   |              |     AIProvider Interface  |   |
|   +-----------+-----------+              +---+-------------------+---+   |
+---------------|------------------------------|-------------------|-------+
                |                              |                   |
        HTTPS / OAuth 2.0              HTTPS / REST        HTTP / REST
                |                              |                   |
                v                              v                   v
     [ Google Gmail API ]              [ OpenAI / Gemini ]  [ Local Ollama ]
```

## 2. Invariant Policies
- **No Inbound Webhook Dependencies for Gmail:** Operates purely on outbound HTTPS polling, functioning behind firewalls and NATs.
- **Provider Interchangeability:** The AI layer operates entirely behind the `AIProvider` interface. The system can run completely offline against a local Ollama instance without code changes.
- **Circuit Breakers & Retries:** External API invocations are wrapped in exponential backoff policies to handle transient outages gracefully.
