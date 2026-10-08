-- ==============================================================================
-- V2__user_profile_skills.sql
-- AI Job Command Center: Phase 2 - User Identity, Profile & Verified Skills
-- ==============================================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
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

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_account_status ON users(account_status);

-- 2. Profiles Table
CREATE TABLE IF NOT EXISTS profiles (
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

CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON profiles(user_id);

-- Profile Child Collections
CREATE TABLE IF NOT EXISTS profile_target_roles (
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    target_role VARCHAR(150) NOT NULL,
    PRIMARY KEY (profile_id, target_role)
);

CREATE TABLE IF NOT EXISTS profile_preferred_locations (
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    preferred_location VARCHAR(150) NOT NULL,
    PRIMARY KEY (profile_id, preferred_location)
);

-- 3. Skills Table (Catalog)
CREATE TABLE IF NOT EXISTS skills (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_skills_normalized_name ON skills(normalized_name);
CREATE INDEX IF NOT EXISTS idx_skills_category ON skills(category);

-- 4. User Skills Table (Candidate's Verified Skills)
CREATE TABLE IF NOT EXISTS user_skills (
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

CREATE INDEX IF NOT EXISTS idx_user_skills_user_id ON user_skills(user_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_skill_id ON user_skills(skill_id);
CREATE INDEX IF NOT EXISTS idx_user_skills_verified ON user_skills(verified);
