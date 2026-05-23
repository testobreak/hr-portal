-- =============================================================================
-- Master data: department, designation, location, employee.
--
-- Conventions (architecture.md §3):
--   - PK: uuid DEFAULT uuidv7().
--   - Audit columns: created_at, created_by, updated_at, updated_by.
--   - Soft delete: deleted_at timestamptz NULL. Reads filter on deleted_at IS NULL.
--   - @Version: bigint NOT NULL DEFAULT 0.
--   - Natural-key uniqueness uses partial unique indexes filtered by
--     (deleted_at IS NULL), so a soft-deleted row never blocks reuse of
--     its code/email.
--   - Email and natural-language codes use citext to be case-insensitive
--     by default.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- department
-- -----------------------------------------------------------------------------
CREATE TABLE department (
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
    CONSTRAINT ck_department_code_nonempty CHECK (length(code) > 0),
    CONSTRAINT ck_department_name_nonempty CHECK (length(name) > 0)
);

CREATE UNIQUE INDEX ux_department_code_live
    ON department (code) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- designation
-- -----------------------------------------------------------------------------
CREATE TABLE designation (
    id          uuid        PRIMARY KEY DEFAULT uuidv7(),
    title       citext      NOT NULL,
    -- Optional band ("L1".."L7", or "JUNIOR".."DIRECTOR"). Free-form for now.
    level       text        NULL,
    description text        NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    created_by  uuid        NULL,
    updated_at  timestamptz NOT NULL DEFAULT now(),
    updated_by  uuid        NULL,
    deleted_at  timestamptz NULL,
    version     bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_designation_title_nonempty CHECK (length(title) > 0)
);

CREATE UNIQUE INDEX ux_designation_title_live
    ON designation (title) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- location
-- -----------------------------------------------------------------------------
CREATE TABLE location (
    id          uuid        PRIMARY KEY DEFAULT uuidv7(),
    code        citext      NOT NULL,
    name        text        NOT NULL,
    city        text        NULL,
    country     text        NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    created_by  uuid        NULL,
    updated_at  timestamptz NOT NULL DEFAULT now(),
    updated_by  uuid        NULL,
    deleted_at  timestamptz NULL,
    version     bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_location_code_nonempty CHECK (length(code) > 0),
    CONSTRAINT ck_location_name_nonempty CHECK (length(name) > 0)
);

CREATE UNIQUE INDEX ux_location_code_live
    ON location (code) WHERE deleted_at IS NULL;

-- -----------------------------------------------------------------------------
-- employee
--
-- Notes:
--   - employee_code is the HR-issued external id (e.g. "ACME-0001").
--   - email is treated as the canonical identity and joined to Keycloak
--     when keycloak_user_id is provisioned.
--   - keycloak_user_id is nullable: an employee record can exist before
--     Keycloak provisioning. Once set, it is the join key to JWT 'sub'.
--   - employment_status is a string enum mirrored in EmploymentStatus.java;
--     the CHECK keeps the DB and the enum in lockstep.
--   - manager_id self-references employee. Recursive CTEs walk the tree.
-- -----------------------------------------------------------------------------
CREATE TABLE employee (
    id                uuid        PRIMARY KEY DEFAULT uuidv7(),
    employee_code     citext      NOT NULL,
    first_name        text        NOT NULL,
    last_name         text        NOT NULL,
    email             citext      NOT NULL,
    phone_number      text        NULL,
    date_of_birth     date        NULL,
    date_of_joining   date        NOT NULL,
    employment_status text        NOT NULL DEFAULT 'ACTIVE',
    keycloak_user_id  uuid        NULL,
    department_id     uuid        NULL REFERENCES department(id)  ON DELETE RESTRICT,
    designation_id    uuid        NULL REFERENCES designation(id) ON DELETE RESTRICT,
    location_id       uuid        NULL REFERENCES location(id)    ON DELETE RESTRICT,
    manager_id        uuid        NULL REFERENCES employee(id)    ON DELETE SET NULL,
    created_at        timestamptz NOT NULL DEFAULT now(),
    created_by        uuid        NULL,
    updated_at        timestamptz NOT NULL DEFAULT now(),
    updated_by        uuid        NULL,
    deleted_at        timestamptz NULL,
    version           bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_employee_code_nonempty       CHECK (length(employee_code) > 0),
    CONSTRAINT ck_employee_first_name_nonempty CHECK (length(first_name)    > 0),
    CONSTRAINT ck_employee_last_name_nonempty  CHECK (length(last_name)     > 0),
    CONSTRAINT ck_employee_email_format        CHECK (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    CONSTRAINT ck_employee_status              CHECK (
        employment_status IN ('ACTIVE','ON_LEAVE','TERMINATED','RESIGNED','ABSCONDED')
    ),
    CONSTRAINT ck_employee_not_self_manager    CHECK (manager_id IS NULL OR manager_id <> id),
    CONSTRAINT ck_employee_dob_past            CHECK (date_of_birth   IS NULL OR date_of_birth   <  current_date),
    CONSTRAINT ck_employee_doj_not_future      CHECK (date_of_joining <= current_date)
);

CREATE UNIQUE INDEX ux_employee_code_live
    ON employee (employee_code) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_employee_email_live
    ON employee (email) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_employee_keycloak_user_id_live
    ON employee (keycloak_user_id)
    WHERE deleted_at IS NULL AND keycloak_user_id IS NOT NULL;

CREATE INDEX ix_employee_department  ON employee (department_id)  WHERE deleted_at IS NULL;
CREATE INDEX ix_employee_designation ON employee (designation_id) WHERE deleted_at IS NULL;
CREATE INDEX ix_employee_location    ON employee (location_id)    WHERE deleted_at IS NULL;
CREATE INDEX ix_employee_manager     ON employee (manager_id)     WHERE deleted_at IS NULL;
CREATE INDEX ix_employee_status      ON employee (employment_status) WHERE deleted_at IS NULL;
