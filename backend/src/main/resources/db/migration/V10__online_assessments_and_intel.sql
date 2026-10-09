-- ==============================================================================
-- V10__online_assessments_and_intel.sql
-- AI Job Command Center: Phase 10 - Online Assessments & Company Intelligence
-- ==============================================================================

-- 1. Online Assessments Table
CREATE TABLE IF NOT EXISTS online_assessments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    application_id UUID REFERENCES job_applications(id) ON DELETE SET NULL,
    interview_id UUID REFERENCES interviews(id) ON DELETE SET NULL,
    platform VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'INVITED',
    result VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    duration_minutes INT,
    invited_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    scheduled_start_time TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    score NUMERIC(6, 2),
    max_score NUMERIC(6, 2),
    assessment_url VARCHAR(500),
    access_code VARCHAR(100),
    submission_notes TEXT,
    submission_repo_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_oa_expiry CHECK (expires_at IS NULL OR expires_at >= invited_at),
    CONSTRAINT chk_oa_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0),
    CONSTRAINT chk_oa_scores CHECK (score IS NULL OR (score >= 0 AND (max_score IS NULL OR score <= max_score))),
    CONSTRAINT chk_oa_max_score CHECK (max_score IS NULL OR max_score > 0)
);

CREATE INDEX IF NOT EXISTS idx_online_assessments_user ON online_assessments(user_id);
CREATE INDEX IF NOT EXISTS idx_online_assessments_job ON online_assessments(job_id);
CREATE INDEX IF NOT EXISTS idx_online_assessments_app ON online_assessments(application_id);
CREATE INDEX IF NOT EXISTS idx_online_assessments_interview ON online_assessments(interview_id);
CREATE INDEX IF NOT EXISTS idx_online_assessments_status ON online_assessments(status);
CREATE INDEX IF NOT EXISTS idx_online_assessments_expires ON online_assessments(expires_at);

-- 2. Online Assessment Events (Immutable Timeline Audit Log)
CREATE TABLE IF NOT EXISTS online_assessment_events (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES online_assessments(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    notes TEXT,
    source VARCHAR(50) NOT NULL DEFAULT 'USER',
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_oa_events_assessment_id ON online_assessment_events(assessment_id);
CREATE INDEX IF NOT EXISTS idx_oa_events_occurred_at ON online_assessment_events(occurred_at DESC);

-- 3. Online Assessment Study Checklists
CREATE TABLE IF NOT EXISTS online_assessment_checklists (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES online_assessments(id) ON DELETE CASCADE,
    topic_category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_oa_checklists_assessment_id ON online_assessment_checklists(assessment_id);

-- 4. Company Dossiers (Technical Briefings & Intelligence)
CREATE TABLE IF NOT EXISTS company_dossiers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    company_tier VARCHAR(50),
    overview TEXT,
    engineering_scale TEXT,
    core_tech_stack TEXT,
    engineering_culture TEXT,
    architecture_focus TEXT,
    tailored_talking_points TEXT,
    interviewer_questions TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_company_dossiers_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_company_dossiers_user ON company_dossiers(user_id);
CREATE INDEX IF NOT EXISTS idx_company_dossiers_job ON company_dossiers(job_id);
