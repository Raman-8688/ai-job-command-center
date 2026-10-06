# Unit Testing Standards

## 1. Scope & Frameworks
- **Frameworks:** JUnit 5, Mockito, AssertJ.
- **Execution Speed:** Fast in-memory execution (<10s for full unit test suite).

## 2. Mandatory Unit Test Coverage Areas
1. **Fit Scoring Algorithm:** Verifies mathematical correctness across edge cases (0% match, 100% match, missing seniority, bonus skills).
2. **Deduplication Hashing:** Verifies that varying URL query parameters, whitespace, or capitalization produce identical canonical hashes.
3. **Application State Machine:** Tests valid and forbidden state transitions (e.g., cannot transition directly from `DISCOVERED` to `OFFER`).
4. **Follow-Up Rule Engine:** Verifies date delta calculations and suppression rules.
5. **JSON Schema Repair:** Tests malformed LLM responses, truncated strings, and missing attributes.
