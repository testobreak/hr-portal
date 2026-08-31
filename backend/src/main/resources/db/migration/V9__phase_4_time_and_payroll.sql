-- =============================================================================
-- Flyway Migration V9: Phase 4 Time & Payroll
-- =============================================================================

-- 1. Attendance Record
CREATE TABLE attendance_record (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    clock_in           timestamptz NOT NULL,
    clock_out          timestamptz NULL,
    status             text        NOT NULL, -- PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE
    working_hours      numeric(5,2) NULL,
    ip_address         text        NULL,
    notes              text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'ON_LEAVE'))
);

CREATE INDEX ix_attendance_record_tenant_emp ON attendance_record(tenant_id, employee_id);
CREATE INDEX ix_attendance_record_clock_in ON attendance_record(clock_in DESC);

-- 2. Timesheet
CREATE TABLE timesheet (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    start_date         date        NOT NULL,
    end_date           date        NOT NULL,
    total_hours        numeric(6,2) NOT NULL DEFAULT 0,
    status             text        NOT NULL, -- DRAFT, SUBMITTED, APPROVED, REJECTED
    approved_by        uuid        NULL REFERENCES employee(id) ON DELETE SET NULL,
    approved_at        timestamptz NULL,
    submission_comments text       NULL,
    approval_comments   text       NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_timesheet_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED'))
);

CREATE INDEX ix_timesheet_tenant_emp ON timesheet(tenant_id, employee_id);

-- 3. Timesheet Line
CREATE TABLE timesheet_line (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    timesheet_id       uuid        NOT NULL REFERENCES timesheet(id) ON DELETE CASCADE,
    day_date           date        NOT NULL,
    hours_worked       numeric(4,2) NOT NULL DEFAULT 0,
    notes              text        NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0
);

CREATE INDEX ix_timesheet_line_sheet ON timesheet_line(timesheet_id);

-- 4. Payroll Run
CREATE TABLE payroll_run (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    period_start       date        NOT NULL,
    period_end         date        NOT NULL,
    status             text        NOT NULL, -- DRAFT, COMPUTING, COMPLETED, APPROVED, PAID, CANCELLED
    payout_date        date        NULL,
    total_gross        numeric(18,4) NOT NULL DEFAULT 0,
    total_deductions   numeric(18,4) NOT NULL DEFAULT 0,
    total_net          numeric(18,4) NOT NULL DEFAULT 0,
    run_type           text        NOT NULL, -- REGULAR, OFF_CYCLE
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_payroll_run_status CHECK (status IN ('DRAFT', 'COMPUTING', 'COMPLETED', 'APPROVED', 'PAID', 'CANCELLED')),
    CONSTRAINT ck_payroll_run_type CHECK (run_type IN ('REGULAR', 'OFF_CYCLE'))
);

CREATE INDEX ix_payroll_run_tenant ON payroll_run(tenant_id);

-- 5. Payslip
CREATE TABLE payslip (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    payroll_run_id     uuid        NOT NULL REFERENCES payroll_run(id) ON DELETE RESTRICT,
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    basic_salary       numeric(18,4) NOT NULL DEFAULT 0,
    allowances         numeric(18,4) NOT NULL DEFAULT 0,
    deductions         numeric(18,4) NOT NULL DEFAULT 0,
    tax_deductions     numeric(18,4) NOT NULL DEFAULT 0,
    net_salary         numeric(18,4) NOT NULL DEFAULT 0,
    working_days       integer     NOT NULL DEFAULT 0,
    present_days       integer     NOT NULL DEFAULT 0,
    leave_days         integer     NOT NULL DEFAULT 0,
    currency_code      varchar(3)     NOT NULL DEFAULT 'USD',
    status             text        NOT NULL, -- DRAFT, APPROVED, PAID
    sent_at            timestamptz NULL,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_payslip_status CHECK (status IN ('DRAFT', 'APPROVED', 'PAID')),
    CONSTRAINT ck_payslip_currency_len CHECK (char_length(currency_code) = 3)
);

CREATE INDEX ix_payslip_run ON payslip(payroll_run_id);
CREATE INDEX ix_payslip_tenant_emp ON payslip(tenant_id, employee_id);

-- 6. Payslip Item
CREATE TABLE payslip_item (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    tenant_id          uuid        NOT NULL,
    payslip_id         uuid        NOT NULL REFERENCES payslip(id) ON DELETE CASCADE,
    item_name          text        NOT NULL,
    item_type          text        NOT NULL, -- ALLOWANCE, DEDUCTION, TAX
    amount             numeric(18,4) NOT NULL DEFAULT 0,
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_payslip_item_type CHECK (item_type IN ('ALLOWANCE', 'DEDUCTION', 'TAX'))
);

CREATE INDEX ix_payslip_item_payslip ON payslip_item(payslip_id);
