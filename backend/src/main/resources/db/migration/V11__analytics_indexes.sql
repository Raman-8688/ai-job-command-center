-- ==============================================================================
-- V11__analytics_indexes.sql
-- AI Job Command Center: Phase 11 - Analytics, Funnel Metrics & Strategic Career KPIs
-- ==============================================================================

-- 1. Composite Index for Job Application Funnel Queries
-- Speeds up filtering and grouping applications by tenant (user_id) and lifecycle status.
CREATE INDEX IF NOT EXISTS idx_job_applications_user_status
    ON job_applications(user_id, status);

-- Speeds up source effectiveness queries grouped by user and application submission source.
CREATE INDEX IF NOT EXISTS idx_job_applications_user_source
    ON job_applications(user_id, submission_source);

-- 2. Composite Index for Interview Conversion & Outcome Metrics
-- Speeds up tenant queries aggregating interview performance across rounds and outcomes.
CREATE INDEX IF NOT EXISTS idx_interviews_user_round_outcome
    ON interviews(user_id, round, outcome);

-- 3. Composite Index for Online Assessment Pass Rates & Lifecycle Status
-- Speeds up tenant queries aggregating assessment completion, status and results.
CREATE INDEX IF NOT EXISTS idx_online_assessments_user_status_result
    ON online_assessments(user_id, status, result);
