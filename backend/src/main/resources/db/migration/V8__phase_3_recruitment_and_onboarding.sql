-- =============================================================================
-- Flyway Migration V8: Phase 3 Recruitment & Onboarding
-- =============================================================================

-- 1. Job Requisition & Openings
CREATE TABLE job_requisition (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    req_number         text        NOT NULL,
    job_title          text        NOT NULL,
    department_id      uuid        NULL REFERENCES department(id) ON DELETE SET NULL,
    designation_id     uuid        NULL REFERENCES designation(id) ON DELETE SET NULL,
    location_id        uuid        NULL REFERENCES location(id) ON DELETE SET NULL,
    legal_entity_id    uuid        NULL REFERENCES legal_entity(id) ON DELETE SET NULL,
    employment_type    text        NOT NULL, -- FULL_TIME, PART_TIME, CONTRACTOR, INTERN
    openings_count     integer     NOT NULL DEFAULT 1,
    hiring_manager_id  uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    recruiter_id       uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    target_start_date  date        NULL,
    min_salary         numeric     NULL,
    max_salary         numeric     NULL,
    currency_code     varchar(3)     NULL DEFAULT 'USD',
    required_skills    text        NULL,
    min_experience_years integer   NULL,
    description        text        NULL,
    justification      text        NULL,
    status             text        NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, OPEN, ON_HOLD, CLOSED, CANCELLED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_job_requisition_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'OPEN', 'ON_HOLD', 'CLOSED', 'CANCELLED'))
);

CREATE INDEX ix_job_requisition_tenant ON job_requisition(tenant_id);

CREATE TABLE job_opening (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    job_requisition_id uuid        NOT NULL REFERENCES job_requisition(id) ON DELETE RESTRICT,
    status             text        NOT NULL, -- OPEN, ON_HOLD, CLOSED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_job_opening_status CHECK (status IN ('OPEN', 'ON_HOLD', 'CLOSED'))
);

CREATE INDEX ix_job_opening_tenant_req ON job_opening(tenant_id, job_requisition_id);

-- 2. Job Posting & Careers Board
CREATE TABLE job_posting (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    job_opening_id     uuid        NOT NULL REFERENCES job_opening(id) ON DELETE CASCADE,
    public_id          uuid        NOT NULL DEFAULT uuidv7(),
    title              text        NOT NULL,
    description        text        NOT NULL,
    location_name      text        NULL,
    work_arrangement   text        NOT NULL, -- REMOTE, ONSITE, HYBRID
    employment_type    text        NOT NULL,
    application_deadline date      NULL,
    status             text        NOT NULL, -- DRAFT, PUBLISHED, UNPUBLISHED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_job_posting_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED'))
);

CREATE UNIQUE INDEX ux_job_posting_public ON job_posting(public_id);
CREATE INDEX ix_job_posting_tenant ON job_posting(tenant_id);

-- 3. Candidate & Candidate Applications
CREATE TABLE candidate (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    first_name         text        NOT NULL,
    last_name          text        NOT NULL,
    email              text        NOT NULL,
    phone              text        NULL,
    resume_storage_key text        NULL,
    skills             text        NULL,
    profile_summary    text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ux_candidate_email_tenant UNIQUE (tenant_id, email)
);

CREATE TABLE candidate_application (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    candidate_id       uuid        NOT NULL REFERENCES candidate(id) ON DELETE CASCADE,
    job_opening_id     uuid        NOT NULL REFERENCES job_opening(id) ON DELETE CASCADE,
    current_stage      text        NOT NULL, -- APPLIED, SCREENING, TECHNICAL_INTERVIEW, HR_ROUND, OFFER, HIRED, REJECTED
    status             text        NOT NULL, -- APPLIED, ACTIVE, REJECTED, WITHDRAWN, HIRED, ARCHIVED
    source             text        NULL,
    cover_letter       text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_candidate_app_status CHECK (status IN ('APPLIED', 'ACTIVE', 'REJECTED', 'WITHDRAWN', 'HIRED', 'ARCHIVED'))
);

CREATE INDEX ix_candidate_app_tenant_job ON candidate_application(tenant_id, job_opening_id);

-- 4. Interview & Evaluation
CREATE TABLE interview (
    id                     uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id              uuid        NOT NULL,
    candidate_application_id uuid      NOT NULL REFERENCES candidate_application(id) ON DELETE CASCADE,
    interview_type         text        NOT NULL, -- SCREENING, TECHNICAL, MANAGERIAL, HR
    scheduled_time         timestamptz NOT NULL,
    status                 text        NOT NULL, -- SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    created_at             timestamptz NOT NULL DEFAULT now(),
    created_by             uuid        NULL,
    updated_at             timestamptz NOT NULL DEFAULT now(),
    updated_by             uuid        NULL,
    deleted_at             timestamptz NULL,
    version                bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_interview_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'))
);

CREATE TABLE interview_feedback (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    interview_id       uuid        NOT NULL REFERENCES interview(id) ON DELETE CASCADE,
    interviewer_id     uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    score              integer     NOT NULL,
    recommendation     text        NOT NULL, -- STRONG_HIRE, HIRE, MIXED, NO_HIRE, STRONG_NO_HIRE
    comments           text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_interview_feedback_rec CHECK (recommendation IN ('STRONG_HIRE', 'HIRE', 'MIXED', 'NO_HIRE', 'STRONG_NO_HIRE'))
);

-- 5. Offer & Pre-Hire Bridge
CREATE TABLE offer (
    id                       uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id                uuid        NOT NULL,
    candidate_application_id uuid        NOT NULL REFERENCES candidate_application(id) ON DELETE CASCADE,
    salary_amount            numeric     NOT NULL,
    currency_code            varchar(3)     NOT NULL DEFAULT 'USD',
    start_date               date        NOT NULL,
    status                   text        NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, SENT, ACCEPTED, REJECTED, EXPIRED
    secure_token             uuid        NOT NULL DEFAULT uuidv7(),
    created_at               timestamptz NOT NULL DEFAULT now(),
    created_by               uuid        NULL,
    updated_at               timestamptz NOT NULL DEFAULT now(),
    updated_by               uuid        NULL,
    deleted_at               timestamptz NULL,
    version                  bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_offer_status CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT ux_offer_token UNIQUE (secure_token)
);

CREATE TABLE pre_hire (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    candidate_id       uuid        NOT NULL REFERENCES candidate(id) ON DELETE RESTRICT,
    accepted_offer_id  uuid        NOT NULL REFERENCES offer(id) ON DELETE RESTRICT,
    legal_entity_id    uuid        NOT NULL REFERENCES legal_entity(id) ON DELETE RESTRICT,
    department_id      uuid        NOT NULL REFERENCES department(id) ON DELETE RESTRICT,
    designation_id     uuid        NOT NULL REFERENCES designation(id) ON DELETE RESTRICT,
    location_id        uuid        NOT NULL REFERENCES location(id) ON DELETE RESTRICT,
    manager_id         uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    start_date         date        NOT NULL,
    status             text        NOT NULL, -- CREATED, ONBOARDING_IN_PROGRESS, READY_FOR_ACTIVATION, ACTIVATED, WITHDRAWN
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_pre_hire_status CHECK (status IN ('CREATED', 'ONBOARDING_IN_PROGRESS', 'READY_FOR_ACTIVATION', 'ACTIVATED', 'WITHDRAWN'))
);

CREATE INDEX ix_pre_hire_tenant ON pre_hire(tenant_id);

-- 6. Onboarding Plans, Tasks, Documents, Assets
CREATE TABLE onboarding_plan (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    pre_hire_id        uuid        NOT NULL REFERENCES pre_hire(id) ON DELETE CASCADE,
    template_name      text        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE onboarding_task (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    onboarding_plan_id uuid        NOT NULL REFERENCES onboarding_plan(id) ON DELETE CASCADE,
    task_name          text        NOT NULL,
    description        text        NULL,
    assigned_role      text        NOT NULL, -- CANDIDATE, HR, IT, MANAGER
    status             text        NOT NULL, -- NOT_STARTED, IN_PROGRESS, COMPLETED, WAIVED
    due_date           date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_onboarding_task_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'WAIVED'))
);

CREATE TABLE onboarding_document (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    pre_hire_id        uuid        NOT NULL REFERENCES pre_hire(id) ON DELETE CASCADE,
    document_type      text        NOT NULL,
    storage_key        text        NULL,
    status             text        NOT NULL, -- REQUESTED, UPLOADED, ACCEPTED, REJECTED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_onboarding_doc_status CHECK (status IN ('REQUESTED', 'UPLOADED', 'ACCEPTED', 'REJECTED'))
);

CREATE TABLE background_check (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    pre_hire_id        uuid        NOT NULL REFERENCES pre_hire(id) ON DELETE CASCADE,
    status             text        NOT NULL, -- NOT_STARTED, IN_PROGRESS, CLEARED, FAILED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_bg_check_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'CLEARED', 'FAILED'))
);

CREATE TABLE asset_request (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    pre_hire_id        uuid        NOT NULL REFERENCES pre_hire(id) ON DELETE CASCADE,
    asset_type         text        NOT NULL,
    status             text        NOT NULL, -- REQUESTED, RESERVED, DELIVERED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_asset_req_status CHECK (status IN ('REQUESTED', 'RESERVED', 'DELIVERED'))
);
