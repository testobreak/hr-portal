-- =============================================================================
-- Flyway Migration V7: Phase 2 Employee & Manager Operating System
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Profile Configuration & Self Service
-- -----------------------------------------------------------------------------
CREATE TABLE profile_field_definition (
    tenant_id          uuid         NOT NULL,
    field_key          text         NOT NULL,
    display_name       text         NOT NULL,
    employee_visible   boolean      NOT NULL DEFAULT true,
    employee_editable  boolean      NOT NULL DEFAULT false,
    manager_visible    boolean      NOT NULL DEFAULT true,
    hr_visible         boolean      NOT NULL DEFAULT true,
    approval_required  boolean      NOT NULL DEFAULT false,
    masking_policy     text         NOT NULL DEFAULT 'NONE',
    PRIMARY KEY (tenant_id, field_key)
);

CREATE TABLE profile_change_request (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    status             text        NOT NULL, -- PENDING, APPROVED, REJECTED, CANCELLED
    requested_by       uuid        NOT NULL,
    requested_at       timestamptz NOT NULL DEFAULT now(),
    approved_by        uuid        NULL,
    approved_at        timestamptz NULL,
    change_json        jsonb       NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_profile_change_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX ix_profile_change_request_tenant_emp ON profile_change_request(tenant_id, employee_id);

CREATE TABLE employee_emergency_contact (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    name               text        NOT NULL,
    relationship       text        NOT NULL,
    phone              text        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE employee_dependent (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    name               text        NOT NULL,
    relationship       text        NOT NULL,
    date_of_birth      date        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE employee_education (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    institution        text        NOT NULL,
    degree             text        NOT NULL,
    year_of_passing    integer     NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE employee_experience (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    company_name       text        NOT NULL,
    role               text        NOT NULL,
    start_date         date        NOT NULL,
    end_date           date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE employee_skill (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    skill_name         text        NOT NULL,
    proficiency        text        NOT NULL, -- BEGINNER, INTERMEDIATE, EXPERT
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE employee_certification (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    certification_name text        NOT NULL,
    issuer             text        NOT NULL,
    expiry_date        date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

-- -----------------------------------------------------------------------------
-- 2. Manager Hierarchy & Delegation
-- -----------------------------------------------------------------------------
CREATE TABLE manager_hierarchy_projection (
    tenant_id             uuid    NOT NULL,
    manager_id            uuid    NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    subordinate_id        uuid    NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    depth                 integer NOT NULL,
    effective_from        date    NOT NULL,
    effective_to          date    NULL,
    PRIMARY KEY (tenant_id, manager_id, subordinate_id, effective_from)
);

CREATE TABLE manager_delegation (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    manager_id         uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    delegate_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    start_date         date        NOT NULL,
    end_date           date        NOT NULL,
    status             text        NOT NULL, -- ACTIVE, REVOKED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_manager_delegation_status CHECK (status IN ('ACTIVE', 'REVOKED'))
);

CREATE INDEX ix_manager_delegation_tenant_mgr ON manager_delegation(tenant_id, manager_id);

-- -----------------------------------------------------------------------------
-- 3. Leave Policies & Ledger & Requests
-- -----------------------------------------------------------------------------
CREATE TABLE leave_type (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    code               text        NOT NULL,
    name               text        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ux_leave_type_code UNIQUE (tenant_id, code)
);

CREATE TABLE leave_policy (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    name               text        NOT NULL,
    description        text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE leave_policy_version (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    policy_id          uuid        NOT NULL REFERENCES leave_policy(id) ON DELETE CASCADE,
    version_number     integer     NOT NULL,
    effective_from     date        NOT NULL,
    effective_to       date        NULL,
    rules_json         jsonb       NOT NULL, -- contains accrualRate, carryForwardMax, negativeBalanceLimit, noticeDays
    status             text        NOT NULL, -- DRAFT, PUBLISHED, RETIRED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_leave_policy_version_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'RETIRED'))
);

CREATE TABLE leave_policy_assignment (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    policy_version_id  uuid        NOT NULL REFERENCES leave_policy_version(id) ON DELETE RESTRICT,
    effective_from     date        NOT NULL,
    effective_to       date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE holiday_calendar (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    name               text        NOT NULL,
    country            text        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE holiday (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    calendar_id        uuid        NOT NULL REFERENCES holiday_calendar(id) ON DELETE CASCADE,
    name               text        NOT NULL,
    holiday_date       date        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ux_holiday_date UNIQUE (tenant_id, calendar_id, holiday_date)
);

CREATE TABLE work_schedule (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    name               text        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE work_schedule_day (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    schedule_id        uuid        NOT NULL REFERENCES work_schedule(id) ON DELETE CASCADE,
    day_of_week        integer     NOT NULL, -- 1=Monday, 7=Sunday
    hours              numeric(4,2) NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ux_work_schedule_day UNIQUE (tenant_id, schedule_id, day_of_week)
);

CREATE TABLE employee_work_schedule (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    schedule_id        uuid        NOT NULL REFERENCES work_schedule(id) ON DELETE RESTRICT,
    effective_from     date        NOT NULL,
    effective_to       date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE leave_ledger_entry (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    leave_type_id      uuid        NOT NULL REFERENCES leave_type(id) ON DELETE RESTRICT,
    transaction_type   text        NOT NULL, -- OPENING_BALANCE, ACCRUAL, CONSUMPTION, RESERVATION, EXPIRY, ADJUSTMENT
    quantity           numeric(6,2) NOT NULL,
    effective_date     date        NOT NULL,
    source_reference   text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE leave_request (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    leave_type_id      uuid        NOT NULL REFERENCES leave_type(id) ON DELETE RESTRICT,
    start_date         date        NOT NULL,
    end_date           date        NOT NULL,
    status             text        NOT NULL, -- DRAFT, SUBMITTED, PENDING_APPROVAL, APPROVED, REJECTED, CANCEL_REQUESTED, CANCELLED
    reason             text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_leave_request_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCEL_REQUESTED', 'CANCELLED'))
);

CREATE TABLE leave_request_day (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    leave_request_id   uuid        NOT NULL REFERENCES leave_request(id) ON DELETE CASCADE,
    day_date           date        NOT NULL,
    hours              numeric(4,2) NOT NULL
);

CREATE TABLE leave_accrual_run (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    run_date           date        NOT NULL,
    period             text        NOT NULL, -- e.g. 2026-07
    created_at         timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE leave_accrual_run_item (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    run_id             uuid        NOT NULL REFERENCES leave_accrual_run(id) ON DELETE CASCADE,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    leave_type_id      uuid        NOT NULL REFERENCES leave_type(id) ON DELETE CASCADE,
    amount             numeric(6,2) NOT NULL,
    idempotency_key    text        NOT NULL,
    CONSTRAINT ux_accrual_idempotency UNIQUE (tenant_id, idempotency_key)
);

-- -----------------------------------------------------------------------------
-- 4. Documents Category & Extensions
-- -----------------------------------------------------------------------------
CREATE TABLE document_category (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    code               text        NOT NULL,
    name               text        NOT NULL,
    employee_visible   boolean     NOT NULL DEFAULT true,
    employee_editable  boolean     NOT NULL DEFAULT true,
    manager_visible    boolean     NOT NULL DEFAULT false,
    retention_years    integer     NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ux_document_category_code UNIQUE (tenant_id, code)
);

-- Alter employee_document to add verification/category columns
ALTER TABLE employee_document ADD COLUMN classification VARCHAR(50) NULL;
ALTER TABLE employee_document ADD COLUMN verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
ALTER TABLE employee_document ADD COLUMN expiry_date DATE NULL;
ALTER TABLE employee_document ADD COLUMN issued_date DATE NULL;
ALTER TABLE employee_document ADD COLUMN verified_by UUID NULL;
ALTER TABLE employee_document ADD COLUMN verified_at TIMESTAMPTZ NULL;
ALTER TABLE employee_document ADD COLUMN rejection_reason TEXT NULL;

-- -----------------------------------------------------------------------------
-- 5. Announcements with Targeting
-- -----------------------------------------------------------------------------
CREATE TABLE announcement (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    title              text        NOT NULL,
    content            text        NOT NULL,
    published_at       timestamptz NULL,
    expires_at         timestamptz NULL,
    pinned             boolean     NOT NULL DEFAULT false,
    status             text        NOT NULL, -- DRAFT, PUBLISHED, ARCHIVED
    author_id          uuid        NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_announcement_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE announcement_audience_rule (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    announcement_id    uuid        NOT NULL REFERENCES announcement(id) ON DELETE CASCADE,
    legal_entity_id    uuid        NULL REFERENCES legal_entity(id) ON DELETE CASCADE,
    department_id      uuid        NULL REFERENCES department(id) ON DELETE CASCADE,
    location_id        uuid        NULL REFERENCES location(id) ON DELETE CASCADE,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE TABLE announcement_delivery (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    announcement_id    uuid        NOT NULL REFERENCES announcement(id) ON DELETE CASCADE,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    delivered_at       timestamptz NOT NULL DEFAULT now(),
    read_at            timestamptz NULL,
    CONSTRAINT ux_announcement_delivery UNIQUE (tenant_id, announcement_id, employee_id)
);

CREATE TABLE announcement_acknowledgment (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    announcement_id    uuid        NOT NULL REFERENCES announcement(id) ON DELETE CASCADE,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    acknowledged_at    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ux_announcement_ack UNIQUE (tenant_id, announcement_id, employee_id)
);

-- -----------------------------------------------------------------------------
-- 6. Configurable Workflows
-- -----------------------------------------------------------------------------
CREATE TABLE workflow_definition (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    workflow_type      text        NOT NULL, -- EMPLOYEE_CHANGE, LEAVE_REQUEST, PROFILE_CHANGE
    name               text        NOT NULL,
    rules_json         jsonb       NOT NULL, -- conditions, steps mapping approvers
    status             text        NOT NULL, -- DRAFT, PUBLISHED, RETIRED
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_workflow_definition_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'RETIRED'))
);

-- Alter employee to add preferred_name, bank_account_number, and tax_id
ALTER TABLE employee ADD COLUMN preferred_name TEXT NULL;
ALTER TABLE employee ADD COLUMN bank_account_number TEXT NULL;
ALTER TABLE employee ADD COLUMN tax_id TEXT NULL;
