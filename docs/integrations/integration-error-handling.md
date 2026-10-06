# Integration Error Handling & Recovery

## 1. Transient Failures vs. Hard Errors
- **Transient Failures (HTTP 429, 502, 503, 504, SocketTimeouts):** Handled via exponential backoff with jitter (initial retry 2s, max 3 attempts).
- **Hard Authentication Errors (HTTP 401, Invalid Grant):** Immediately flags the integration as `REQUIRES_REAUTHORIZATION`, suspends scheduled background sync jobs, and alerts the user on the dashboard.
- **Provider Quota Depletion (HTTP 402, Quota Exceeded):** Preserves raw items in `FAILED_ANALYSIS` state, pauses automated analysis queues, and suggests switching to local Ollama.

## 2. Dead-Letter Queuing
Tasks that fail after maximum retries are moved to a dead-letter state table (`integration_failures`) with error stack traces and raw payloads for manual replay by the user.
