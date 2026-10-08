# Phase 2 — User Identity, Profile & Verified Skills

## 1. Executive Summary

Phase 2 implements the trusted candidate foundation of the **AI Job Command Center** modular monolith. It provides:
1. **User Identity & JWT Authentication**: Secure user registration, authentication, bcrypt credential hashing, stateless HMAC-SHA512 JWT generation, and token-based security context propagation.
2. **Professional Profile Management**: Independent candidate profile lifecycle maintaining contact information, public presence links, target roles, preferred locations, years of experience, and notice period.
3. **Skill Catalog & Candidate Claim Management**: Standardized skill catalog with normalized uniqueness and categorization.
4. **Anti-Hallucination Grounding (Principle 1)**: Strict business rules ensuring that skills suggested by AI algorithms (`AI_SUGGESTED`) can never automatically be marked as verified (`verified = false`) without explicit human confirmation.

---

## 2. Architecture & Layering

Following the **Feature-Oriented Modular Monolith** architecture:

```text
com.jobcommandcenter
├── common/             # Cross-cutting error responses, correlation ID, logging
├── security/           # JWT filters, token services, SecurityUser UserDetails adapter
├── user/               # User Identity & Authentication module
│   ├── api/            # AuthController, UserController, request/response DTO records
│   ├── application/    # AuthenticationService, UserService
│   ├── domain/         # User, UserRepository, AccountStatus, Role
│   └── infrastructure/ # UserJpaEntity, SpringDataUserRepository, UserPersistenceAdapter
├── profile/            # Candidate Profile module
│   ├── api/            # ProfileController, request/response DTO records
│   ├── application/    # ProfileService
│   ├── domain/         # Profile, ProfileRepository, WorkPreference
│   └── infrastructure/ # ProfileJpaEntity, SpringDataProfileRepository, ProfilePersistenceAdapter
└── skill/              # Skill Catalog & Candidate Skills module
    ├── api/            # SkillController, UserSkillController, request/response DTO records
    ├── application/    # SkillCatalogService, UserSkillService
    ├── domain/         # Skill, UserSkill, SkillRepository, UserSkillRepository, SkillCategory, SkillProficiency, VerificationSource
    └── infrastructure/ # SkillJpaEntity, UserSkillJpaEntity, SpringData repositories, Persistence adapters
```

### Dependency Rules Enforced:
- **API** depends only on **Application** (controllers are thin orchestrators).
- **Application** coordinates use cases and domain models.
- **Domain** contains pure POJOs, business rules, invariants, and repository interfaces. No JPA annotations or Spring framework dependencies.
- **Infrastructure** implements domain repository interfaces via JPA/Spring Data adapters.

---

## 3. Database Schema (Flyway V2)

Migration script: `db/migration/V2__user_profile_skills.sql`

```sql
-- 1. Users
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    account_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- 2. Profiles
CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    phone VARCHAR(50),
    location VARCHAR(255),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    work_preference VARCHAR(50) NOT NULL DEFAULT 'REMOTE',
    years_experience NUMERIC(4, 1) NOT NULL DEFAULT 0.0,
    notice_period_days INT NOT NULL DEFAULT 0,
    current_company VARCHAR(255),
    current_designation VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Profile child collections
CREATE TABLE profile_target_roles (
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    target_role VARCHAR(150) NOT NULL,
    PRIMARY KEY (profile_id, target_role)
);

CREATE TABLE profile_preferred_locations (
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    preferred_location VARCHAR(150) NOT NULL,
    PRIMARY KEY (profile_id, preferred_location)
);

-- 3. Skills Catalog
CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Candidate Skills
CREATE TABLE user_skills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    proficiency VARCHAR(50) NOT NULL,
    years_experience NUMERIC(4, 1) NOT NULL DEFAULT 0.0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_source VARCHAR(50) NOT NULL,
    notes TEXT,
    last_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_skills_user_skill UNIQUE (user_id, skill_id)
);
```

---

## 4. Anti-Hallucination Grounding

In compliance with **Core Product Principle 1 (Truthfulness)**:
1. **Domain Guarantee**:
   In `UserSkill`:
   ```java
   if (this.verificationSource == VerificationSource.AI_SUGGESTED && verified) {
       this.verified = false;
       this.lastVerifiedAt = null;
   }
   ```
2. **Explicit Verification Elevation**:
   When an AI-suggested skill claim is verified by the candidate through `updateDetails(...)` or `confirmVerification(...)`, the verification source is elevated to `USER_EXPLICIT` with `lastVerifiedAt` recorded to the exact timestamp of human confirmation.
3. **No Automatic Prompts**:
   Downstream AI modules (Job Analysis, Resume Tailoring, Cover Letters) are guaranteed to receive `verified` booleans and verification sources, preventing false claims on generated resumes.

---

## 5. API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/login` | Authenticate user credentials and issue JWT | No |
| `GET` | `/api/users/me` | Fetch authenticated user identity details | Yes (Bearer) |
| `GET` | `/api/profile` | Retrieve candidate professional profile (auto-initializes if new) | Yes (Bearer) |
| `PUT` | `/api/profile` | Update candidate professional profile | Yes (Bearer) |
| `GET` | `/api/skills` | Search standardized skill catalog (query & category filters) | Yes (Bearer) |
| `GET` | `/api/skills/{id}` | Get catalog skill by ID | Yes (Bearer) |
| `POST` | `/api/skills` | Add new skill to catalog (enforces normalized uniqueness) | Yes (Bearer) |
| `GET` | `/api/profile/skills` | List authenticated candidate's skills claims | Yes (Bearer) |
| `POST` | `/api/profile/skills` | Claim a skill (enforces anti-hallucination verification) | Yes (Bearer) |
| `PUT` | `/api/profile/skills/{id}` | Update candidate skill claim | Yes (Bearer) |
| `DELETE` | `/api/profile/skills/{id}` | Remove candidate skill claim | Yes (Bearer) |

---

## 6. Verification and Testing

Automated test suite (`mvn test`) runs 39 tests across unit and integration levels with 100% pass rate:
- `UserAuthenticationIntegrationTest`: Credential authentication, token issuance, locked/inactive account rejection, `/api/users/me` retrieval.
- `ProfileIntegrationTest`: Profile retrieval, auto-creation, updates with multi-valued collections.
- `SkillAndAntiHallucinationIntegrationTest`: Catalog uniqueness, AI suggestion unverified enforcement, user verification promotion, tenant isolation.
- `UserSkillUnitTest`: Domain-level anti-hallucination invariants and state transitions.
- `DatabaseFlywayIntegrationTest` & `PostgreSQLConnectionIntegrationTest`: V1 and V2 migrations validated against both H2 in-memory and live PostgreSQL 17.
