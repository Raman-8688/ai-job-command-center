-- ==============================================================================
-- V4__job_ai_analyses.sql
-- AI Job Command Center: Phase 4 - AI Job Analysis & Fit Scoring
-- ==============================================================================

-- 1. Job AI Analyses Table
CREATE TABLE IF NOT EXISTS job_ai_analyses (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    normalized_title VARCHAR(255),
    seniority_level VARCHAR(50),
    experience_expectations VARCHAR(255),
    confidence NUMERIC(3, 2),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_job_ai_analyses_job_version UNIQUE (job_id, version)
);

CREATE INDEX IF NOT EXISTS idx_job_ai_analyses_job_id ON job_ai_analyses(job_id);
CREATE INDEX IF NOT EXISTS idx_job_ai_analyses_status ON job_ai_analyses(status);
CREATE INDEX IF NOT EXISTS idx_job_ai_analyses_created_at ON job_ai_analyses(created_at DESC);

-- 2. Core Responsibilities Collection
CREATE TABLE IF NOT EXISTS job_ai_responsibilities (
    analysis_id UUID NOT NULL REFERENCES job_ai_analyses(id) ON DELETE CASCADE,
    responsibility TEXT NOT NULL,
    is_inferred BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (analysis_id, responsibility)
);

CREATE INDEX IF NOT EXISTS idx_job_ai_resp_analysis_id ON job_ai_responsibilities(analysis_id);

-- 3. Technologies Collection
CREATE TABLE IF NOT EXISTS job_ai_technologies (
    analysis_id UUID NOT NULL REFERENCES job_ai_analyses(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    technology VARCHAR(100) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (analysis_id, category, technology)
);

CREATE INDEX IF NOT EXISTS idx_job_ai_tech_analysis_id ON job_ai_technologies(analysis_id);
CREATE INDEX IF NOT EXISTS idx_job_ai_tech_category ON job_ai_technologies(category);

-- 4. Education & Certification Requirements Collection
CREATE TABLE IF NOT EXISTS job_ai_requirements (
    analysis_id UUID NOT NULL REFERENCES job_ai_analyses(id) ON DELETE CASCADE,
    requirement_type VARCHAR(50) NOT NULL,
    requirement VARCHAR(255) NOT NULL,
    PRIMARY KEY (analysis_id, requirement_type, requirement)
);

CREATE INDEX IF NOT EXISTS idx_job_ai_req_analysis_id ON job_ai_requirements(analysis_id);

-- 5. Potential Red Flags Collection
CREATE TABLE IF NOT EXISTS job_ai_red_flags (
    analysis_id UUID NOT NULL REFERENCES job_ai_analyses(id) ON DELETE CASCADE,
    red_flag TEXT NOT NULL,
    PRIMARY KEY (analysis_id, red_flag)
);

CREATE INDEX IF NOT EXISTS idx_job_ai_flags_analysis_id ON job_ai_red_flags(analysis_id);
