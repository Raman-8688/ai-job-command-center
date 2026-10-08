-- ==============================================================================
-- V5__resume_management.sql
-- AI Job Command Center: Phase 5 - Resume Management & Job-Specific Analysis
-- ==============================================================================

-- 1. Resumes Aggregate Root Table
CREATE TABLE IF NOT EXISTS resumes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    summary TEXT,
    years_of_experience NUMERIC(4, 1),
    location VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON resumes(user_id);
CREATE INDEX IF NOT EXISTS idx_resumes_status ON resumes(status);
CREATE INDEX IF NOT EXISTS idx_resumes_created_at ON resumes(created_at DESC);

-- 2. Resume Work Experience Table
CREATE TABLE IF NOT EXISTS resume_experiences (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    company VARCHAR(255) NOT NULL,
    job_title VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    currently_working BOOLEAN NOT NULL DEFAULT FALSE,
    location VARCHAR(255),
    description TEXT,
    achievements TEXT,
    technologies TEXT,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_exp_resume_id ON resume_experiences(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_exp_display_order ON resume_experiences(display_order);

-- 3. Resume Projects Table
CREATE TABLE IF NOT EXISTS resume_projects (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    project_name VARCHAR(255) NOT NULL,
    description TEXT,
    role VARCHAR(255),
    technologies TEXT,
    responsibilities TEXT,
    achievements TEXT,
    duration VARCHAR(100),
    project_url VARCHAR(1000),
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_proj_resume_id ON resume_projects(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_proj_display_order ON resume_projects(display_order);

-- 4. Resume Skills Table (References Canonical Skills Catalog)
CREATE TABLE IF NOT EXISTS resume_skills (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE RESTRICT,
    proficiency VARCHAR(50),
    years_experience NUMERIC(4, 1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_resume_skills_resume_skill UNIQUE (resume_id, skill_id)
);

CREATE INDEX IF NOT EXISTS idx_resume_skills_resume_id ON resume_skills(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_skills_skill_id ON resume_skills(skill_id);

-- 5. Resume Education Table
CREATE TABLE IF NOT EXISTS resume_education (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    institution VARCHAR(255) NOT NULL,
    degree VARCHAR(255),
    field_of_study VARCHAR(255),
    start_year INT,
    end_year INT,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_edu_resume_id ON resume_education(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_edu_display_order ON resume_education(display_order);

-- 6. Resume Certifications Table
CREATE TABLE IF NOT EXISTS resume_certifications (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    issuing_organization VARCHAR(255) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    credential_id VARCHAR(255),
    credential_url VARCHAR(1000),
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_resume_cert_resume_id ON resume_certifications(resume_id);
CREATE INDEX IF NOT EXISTS idx_resume_cert_display_order ON resume_certifications(display_order);
