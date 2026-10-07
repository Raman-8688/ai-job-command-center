-- ==============================================================================
-- V1__baseline.sql
-- AI Job Command Center: Database Infrastructure Baseline
-- Phase 1 Foundation: Verifies Flyway execution and system metadata tracking
-- (No business tables created in Phase 1)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS system_metadata (
    metadata_key VARCHAR(100) PRIMARY KEY,
    metadata_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (metadata_key, metadata_value)
VALUES ('schema_version', '1.0.0');

INSERT INTO system_metadata (metadata_key, metadata_value)
VALUES ('phase', '1_backend_foundation');
