-- =============================================================================
-- Migration V6: Introduce multi-tenancy and the first vertical slice tables.
-- =============================================================================

-- 1. Add tenant_id column to existing tables with default system uuid
ALTER TABLE department       ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE designation      ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE location         ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE employee         ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE salary_history   ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE employee_document ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE client           ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE project          ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE allocation       ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;
ALTER TABLE audit_log        ADD COLUMN tenant_id uuid NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'::uuid;

-- Remove defaults to force application/filter supply of tenant_id hereafter
ALTER TABLE department       ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE designation      ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE location         ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE employee         ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE salary_history   ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE employee_document ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE client           ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE project          ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE allocation       ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE audit_log        ALTER COLUMN tenant_id DROP DEFAULT;

-- 2. Drop globally unique indexes and recreate them partitioned by tenant_id
DROP INDEX IF EXISTS ux_department_code_live;
CREATE UNIQUE INDEX ux_department_code_live ON department (tenant_id, code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_designation_title_live;
CREATE UNIQUE INDEX ux_designation_title_live ON designation (tenant_id, title) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_location_code_live;
CREATE UNIQUE INDEX ux_location_code_live ON location (tenant_id, code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_employee_code_live;
CREATE UNIQUE INDEX ux_employee_code_live ON employee (tenant_id, employee_code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_employee_email_live;
CREATE UNIQUE INDEX ux_employee_email_live ON employee (tenant_id, email) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_client_code_live;
CREATE UNIQUE INDEX ux_client_code_live ON client (tenant_id, code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_project_code_live;
CREATE UNIQUE INDEX ux_project_code_live ON project (tenant_id, project_code) WHERE deleted_at IS NULL;

DROP INDEX IF EXISTS ux_employee_document_storage_key_live;
CREATE UNIQUE INDEX ux_employee_document_storage_key_live ON employee_document (tenant_id, storage_key) WHERE deleted_at IS NULL;

-- 3. Create legal_entity table
CREATE TABLE legal_entity (
    id          uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id   uuid        NOT NULL,
    code        citext      NOT NULL,
    name        text        NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    created_by  uuid        NULL,
    updated_at  timestamptz NOT NULL DEFAULT now(),
    updated_by  uuid        NULL,
    deleted_at  timestamptz NULL,
    version     bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_legal_entity_code_nonempty CHECK (length(code) > 0),
    CONSTRAINT ck_legal_entity_name_nonempty CHECK (length(name) > 0)
);

CREATE UNIQUE INDEX ux_legal_entity_code_live ON legal_entity (tenant_id, code) WHERE deleted_at IS NULL;

-- 4. Add legal_entity_id relation to employee
ALTER TABLE employee ADD COLUMN legal_entity_id uuid NULL REFERENCES legal_entity(id) ON DELETE RESTRICT;
CREATE INDEX ix_employee_legal_entity ON employee (legal_entity_id) WHERE deleted_at IS NULL;

-- 5. Create employee_assignment_history for tracking effective-dated changes
CREATE TABLE employee_assignment_history (
    id             uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id      uuid        NOT NULL,
    employee_id    uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    department_id  uuid        NULL REFERENCES department(id) ON DELETE RESTRICT,
    designation_id uuid        NULL REFERENCES designation(id) ON DELETE RESTRICT,
    location_id    uuid        NULL REFERENCES location(id) ON DELETE RESTRICT,
    manager_id     uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    effective_from date        NOT NULL,
    effective_to   date        NULL,
    created_at     timestamptz NOT NULL DEFAULT now(),
    created_by     uuid        NULL,
    updated_at     timestamptz NOT NULL DEFAULT now(),
    updated_by     uuid        NULL,
    deleted_at     timestamptz NULL,
    version        bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_assignment_date_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ix_assignment_history_employee ON employee_assignment_history (employee_id, effective_from DESC) WHERE deleted_at IS NULL;

-- 6. Create approval_request for manager change requests
CREATE TABLE approval_request (
    id             uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id      uuid        NOT NULL,
    requester_id   uuid        NOT NULL,
    employee_id    uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    type           text        NOT NULL,
    change_json    jsonb       NOT NULL,
    status         text        NOT NULL,
    approved_by    uuid        NULL,
    approved_at    timestamptz NULL,
    created_at     timestamptz NOT NULL DEFAULT now(),
    created_by     uuid        NULL,
    updated_at     timestamptz NOT NULL DEFAULT now(),
    updated_by     uuid        NULL,
    deleted_at     timestamptz NULL,
    version        bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_approval_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX ix_approval_request_employee ON approval_request (employee_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_approval_request_status ON approval_request (status) WHERE deleted_at IS NULL;

-- 7. Create outbox_event table for transactional event propagation
CREATE TABLE outbox_event (
    id             uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id      uuid        NOT NULL,
    event_type     text        NOT NULL,
    payload        jsonb       NOT NULL,
    status         text        NOT NULL DEFAULT 'PENDING',
    created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_outbox_event_status ON outbox_event (status, created_at);
