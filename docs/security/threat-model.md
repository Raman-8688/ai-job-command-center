# Threat Model

| Threat Identifier | Threat Description | Attack Vector | Severity | Mitigation Strategy |
|---|---|---|---|---|
| **TM-01** | **Accidental Secret Leakage** | Committing `.env` or OAuth credentials to Git. | Critical | Strict `.gitignore`, CI scanning, `.env.example` templates. |
| **TM-02** | **OAuth Token Theft** | SQL injection or database dump exposing refresh tokens. | High | AES-256-GCM token encryption at rest via PBKDF2 derived keys. |
| **TM-03** | **Prompt Injection via JD / Email** | Malicious text in job posting or recruiter email overriding LLM rules. | High | Strict structural delimiter fencing, JSON schema validation, human approval gates. |
| **TM-04** | **AI Factual Fabrication** | LLM inventing unverified skills on tailored resumes. | High | Factual grounding interceptor checking all tokens against `user_skills`. |
| **TM-05** | **Unauthorized Email Sending** | Bot bug dispatching incorrect replies to real recruiters. | High | Strict Level 3 restriction: no programmatic send without manual UI confirmation. |
| **TM-06** | **CORS / CSRF Hijacking** | Malicious web page accessing local REST API. | Medium | Spring Security CORS strict localhost binding and Bearer token headers. |
