-- =============================================================================
-- Projects: clients, projects, and employee allocations.
--
-- Rate cards are intentionally deferred to the billing / finance phase.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- client
-- -----------------------------------------------------------------------------
CREATE TABLE client (
    id          uuid        PRIMARY KEY DEFAULT uuidv7(),
    code        citext      NOT NULL,
    name        text        NOT NULL,
    description text        NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    created_by  uuid        NULL,
    updated_at  timestamptz NOT NULL DEFAULT now(),
    updated_by  uuid        NULL,
    deleted_at  timestamptz NULL,
    version     bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_client_code_nonempty CHECK (length(code) > 0),
    CONSTRAINT ck_client_name_nonempty CHECK (length(name) > 0)
);

CREATE UNIQUE INDEX ux_client_code_live
    ON client (code) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- project
-- -----------------------------------------------------------------------------
CREATE TABLE project (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    client_id          uuid        NOT NULL REFERENCES client(id)   ON DELETE RESTRICT,
    project_code       citext      NOT NULL,
    name               text        NOT NULL,
    description        text        NULL,
    project_manager_id uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    status             text        NOT NULL DEFAULT 'ACTIVE',
    start_date         date        NOT NULL,
    end_date           date        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_project_code_nonempty CHECK (length(project_code) > 0),
    CONSTRAINT ck_project_name_nonempty CHECK (length(name) > 0),
    CONSTRAINT ck_project_status CHECK (status IN ('PLANNED','ACTIVE','ON_HOLD','COMPLETED','CANCELLED')),
    CONSTRAINT ck_project_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE UNIQUE INDEX ux_project_code_live
    ON project (project_code) WHERE deleted_at IS NULL;

CREATE INDEX ix_project_client
    ON project (client_id) WHERE deleted_at IS NULL;

CREATE INDEX ix_project_manager
    ON project (project_manager_id) WHERE deleted_at IS NULL;

CREATE INDEX ix_project_status
    ON project (status) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- allocation
-- -----------------------------------------------------------------------------
CREATE TABLE allocation (
    id                    uuid        PRIMARY KEY DEFAULT uuidv7(),
    project_id            uuid        NOT NULL REFERENCES project(id)  ON DELETE RESTRICT,
    employee_id           uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    allocation_percentage integer     NOT NULL,
    role_title            text        NULL,
    start_date            date        NOT NULL,
    end_date              date        NULL,
    created_at            timestamptz NOT NULL DEFAULT now(),
    created_by            uuid        NULL,
    updated_at            timestamptz NOT NULL DEFAULT now(),
    updated_by            uuid        NULL,
    deleted_at            timestamptz NULL,
    version               bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_allocation_percentage CHECK (allocation_percentage BETWEEN 1 AND 100),
    CONSTRAINT ck_allocation_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX ix_allocation_project
    ON allocation (project_id) WHERE deleted_at IS NULL;

CREATE INDEX ix_allocation_employee
    ON allocation (employee_id) WHERE deleted_at IS NULL;

CREATE INDEX ix_allocation_active_by_project
    ON allocation (project_id, employee_id, start_date, end_date) WHERE deleted_at IS NULL;
