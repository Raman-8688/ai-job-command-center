-- ==============================================================================
-- V3__jobs_and_matching.sql
-- AI Job Command Center: Phase 3 - Job Discovery, Normalization & Matching
-- ==============================================================================

-- 1. Jobs Table (Canonical Job Postings)
CREATE TABLE IF NOT EXISTS jobs (
    id UUID PRIMARY KEY,
    external_job_id VARCHAR(150),
    title VARCHAR(255) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    company_website VARCHAR(500),
    job_url VARCHAR(1000),
    description TEXT NOT NULL,
    location VARCHAR(255),
    work_mode VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',
    employment_type VARCHAR(50) NOT NULL DEFAULT 'FULL_TIME',
    experience_min_years NUMERIC(4, 1),
    experience_max_years NUMERIC(4, 1),
    salary_min NUMERIC(12, 2),
    salary_max NUMERIC(12, 2),
    salary_currency VARCHAR(10),
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_url VARCHAR(1000),
    posted_at TIMESTAMP WITH TIME ZONE,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    application_deadline TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    deduplication_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jobs_source_ext_id ON jobs(source, external_job_id);
CREATE INDEX IF NOT EXISTS idx_jobs_dedup_hash ON jobs(deduplication_hash);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_company ON jobs(company_name);
CREATE INDEX IF NOT EXISTS idx_jobs_work_mode ON jobs(work_mode);
CREATE INDEX IF NOT EXISTS idx_jobs_created_at ON jobs(created_at DESC);

-- 2. Job Skills Table (Normalized Skill Requirements)
CREATE TABLE IF NOT EXISTS job_skills (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    requirement_type VARCHAR(50) NOT NULL DEFAULT 'REQUIRED',
    years_experience_required NUMERIC(4, 1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_job_skills_job_skill UNIQUE (job_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_job_skills_job_id ON job_skills(job_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_skill_id ON job_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_job_skills_req_type ON job_skills(requirement_type);

-- 3. User Jobs Table (Candidate-Specific Interactions & Privacy Isolation)
CREATE TABLE IF NOT EXISTS user_jobs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'DISCOVERED',
    notes TEXT,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_jobs_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_user_jobs_user_id ON user_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_jobs_job_id ON user_jobs(job_id);
CREATE INDEX IF NOT EXISTS idx_user_jobs_status ON user_jobs(status);
