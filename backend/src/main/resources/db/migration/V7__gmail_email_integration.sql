-- ==============================================================================
-- V7__gmail_email_integration.sql
-- AI Job Command Center: Phase 7 - Gmail / Email Integration & Processing
-- ==============================================================================

-- 1. Email Connections (OAuth and sync metadata per user and provider)
CREATE TABLE IF NOT EXISTS email_connections (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL DEFAULT 'GMAIL',
    email_address VARCHAR(255) NOT NULL,
    is_connected BOOLEAN NOT NULL DEFAULT true,
    access_token TEXT,
    refresh_token TEXT,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    scopes VARCHAR(500),
    last_sync_at TIMESTAMP WITH TIME ZONE,
    last_history_id VARCHAR(100),
    sync_status VARCHAR(50) NOT NULL DEFAULT 'IDLE',
    sync_error_message TEXT,
    emails_synced_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_email_connections_user_provider UNIQUE (user_id, provider)
);

CREATE INDEX IF NOT EXISTS idx_email_connections_user_id ON email_connections(user_id);
CREATE INDEX IF NOT EXISTS idx_email_connections_provider ON email_connections(provider);

-- 2. Emails Table (Synchronized Email Messages)
CREATE TABLE IF NOT EXISTS emails (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    connection_id UUID REFERENCES email_connections(id) ON DELETE SET NULL,
    external_message_id VARCHAR(255) NOT NULL,
    external_thread_id VARCHAR(255),
    sender VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    snippet TEXT,
    body_plain TEXT,
    body_html TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    classification VARCHAR(50) NOT NULL DEFAULT 'UNCLASSIFIED',
    classification_confidence NUMERIC(4, 3),
    classification_reason TEXT,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'UNPROCESSED',
    extracted_company_name VARCHAR(255),
    extracted_job_title VARCHAR(255),
    extracted_external_id VARCHAR(150),
    extracted_notes TEXT,
    associated_job_id UUID REFERENCES jobs(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_emails_user_external_message_id UNIQUE (user_id, external_message_id)
);

CREATE INDEX IF NOT EXISTS idx_emails_user_id ON emails(user_id);
CREATE INDEX IF NOT EXISTS idx_emails_received_at ON emails(received_at DESC);
CREATE INDEX IF NOT EXISTS idx_emails_classification ON emails(classification);
CREATE INDEX IF NOT EXISTS idx_emails_processing_status ON emails(processing_status);
CREATE INDEX IF NOT EXISTS idx_emails_associated_job_id ON emails(associated_job_id);
CREATE INDEX IF NOT EXISTS idx_emails_external_thread_id ON emails(external_thread_id);
