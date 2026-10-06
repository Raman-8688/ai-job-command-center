# AI Cost & Token Management

## 1. Cost Containment Strategy
1. **Tiered Model Routing:**
   - Routine extraction & classification tasks (email categorization, JD keyword extraction) use lightweight, cost-effective models (e.g., `gpt-4o-mini`, `gemini-1.5-flash`).
   - Deep contextual synthesis (custom resume bullet optimization, complex behavioral interview briefs) selectively use flagship models (`gpt-4o`, `claude-3-5-sonnet`).
2. **Context Pruning:**
   - Strip boilerplate HTML and navigation footers from job postings and emails before sending tokens to the LLM.
3. **Local LLM Fallback:**
   - Enable zero-marginal-cost local inference via Ollama for developers running workstation GPUs or Apple Silicon.

## 2. Token Budgeting & Tracking
- Every request persists token counts (`prompt_tokens`, `completion_tokens`) and estimated USD cost.
- A monthly budget alert threshold (e.g., $15.00/month) triggers a warning on the Command Center dashboard if exceeded.
