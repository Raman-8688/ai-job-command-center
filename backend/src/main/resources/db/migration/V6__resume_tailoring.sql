-- ==============================================================================
-- V6__resume_tailoring.sql
-- AI Job Command Center: Phase 6 - Resume Tailoring & Versioning Workflow
-- ==============================================================================

-- 1. Tailored Resumes Aggregate Root Table
CREATE TABLE IF NOT EXISTS tailored_resumes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    target_job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    tailored_title VARCHAR(255),
    tailored_summary TEXT,
    keyword_coverage_score NUMERIC(5, 2),
    matched_keywords TEXT,
    missing_keywords TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tailored_resume_version UNIQUE (source_resume_id, target_job_id, version)
);

CREATE INDEX IF NOT EXISTS idx_tailored_resumes_user_id ON tailored_resumes(user_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_source_job ON tailored_resumes(source_resume_id, target_job_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_status ON tailored_resumes(status);
CREATE INDEX IF NOT EXISTS idx_tailored_resumes_created_at ON tailored_resumes(created_at DESC);

-- 2. Tailored Resume Section Suggestions Table
CREATE TABLE IF NOT EXISTS tailored_resume_suggestions (
    id UUID PRIMARY KEY,
    tailored_resume_id UUID NOT NULL REFERENCES tailored_resumes(id) ON DELETE CASCADE,
    section_type VARCHAR(50) NOT NULL,
    target_item_title VARCHAR(255),
    original_content TEXT,
    suggested_content TEXT NOT NULL,
    rationale TEXT NOT NULL,
    evidence TEXT,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    applied BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tailored_resume_sug_resume_id ON tailored_resume_suggestions(tailored_resume_id);
CREATE INDEX IF NOT EXISTS idx_tailored_resume_sug_section ON tailored_resume_suggestions(section_type);
