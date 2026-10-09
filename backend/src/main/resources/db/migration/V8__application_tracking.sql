-- ==============================================================================
-- V8__application_tracking.sql
-- AI Job Command Center: Phase 8 - Application Tracking & Lifecycle Management
-- ==============================================================================

-- 1. Job Applications Table
CREATE TABLE IF NOT EXISTS job_applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    resume_id UUID REFERENCES resumes(id) ON DELETE SET NULL,
    tailored_resume_id UUID REFERENCES tailored_resumes(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    applied_at TIMESTAMP WITH TIME ZONE,
    submission_source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    external_reference VARCHAR(150),
    next_follow_up_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_job_applications_user_job UNIQUE (user_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_job_applications_user ON job_applications(user_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_job ON job_applications(job_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_status ON job_applications(status);
CREATE INDEX IF NOT EXISTS idx_job_applications_applied_at ON job_applications(applied_at DESC);
CREATE INDEX IF NOT EXISTS idx_job_applications_follow_up ON job_applications(next_follow_up_date);

-- 2. Job Application Events (Immutable Timeline Audit Log)
CREATE TABLE IF NOT EXISTS job_application_events (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    notes TEXT,
    source VARCHAR(50) NOT NULL DEFAULT 'USER',
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_app_events_app_id ON job_application_events(application_id);
CREATE INDEX IF NOT EXISTS idx_job_app_events_occurred_at ON job_application_events(occurred_at DESC);
