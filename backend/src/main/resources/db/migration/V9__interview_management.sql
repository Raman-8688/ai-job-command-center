-- ==============================================================================
-- V9__interview_management.sql
-- AI Job Command Center: Phase 9 - Interview Management & Preparation Lifecycle
-- ==============================================================================

-- 1. Interviews Table
CREATE TABLE IF NOT EXISTS interviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id UUID REFERENCES job_applications(id) ON DELETE SET NULL,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    round VARCHAR(50) NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    format VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    outcome VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    scheduled_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    time_zone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    meeting_link VARCHAR(500),
    location VARCHAR(255),
    interviewer_names VARCHAR(255),
    interviewer_roles VARCHAR(255),
    notes TEXT,
    candidate_feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_interview_time_window CHECK (scheduled_end_time > scheduled_start_time)
);

CREATE INDEX IF NOT EXISTS idx_interviews_user ON interviews(user_id);
CREATE INDEX IF NOT EXISTS idx_interviews_app ON interviews(application_id);
CREATE INDEX IF NOT EXISTS idx_interviews_job ON interviews(job_id);
CREATE INDEX IF NOT EXISTS idx_interviews_status ON interviews(status);
CREATE INDEX IF NOT EXISTS idx_interviews_scheduled_start ON interviews(scheduled_start_time);

-- 2. Interview Events (Timeline Audit Log)
CREATE TABLE IF NOT EXISTS interview_events (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    notes TEXT,
    source VARCHAR(50) NOT NULL DEFAULT 'USER',
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_interview_events_interview_id ON interview_events(interview_id);
CREATE INDEX IF NOT EXISTS idx_interview_events_occurred_at ON interview_events(occurred_at DESC);

-- 3. Interview Preparations (AI Question Generation & STAR Talking Points)
CREATE TABLE IF NOT EXISTS interview_preparations (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    topic_category VARCHAR(50) NOT NULL,
    question TEXT NOT NULL,
    talking_points TEXT,
    suggested_answer_star TEXT,
    user_answer_notes TEXT,
    confidence_score NUMERIC(3,2) DEFAULT 0.85,
    is_reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_interview_preps_interview_id ON interview_preparations(interview_id);
CREATE INDEX IF NOT EXISTS idx_interview_preps_topic ON interview_preparations(topic_category);
