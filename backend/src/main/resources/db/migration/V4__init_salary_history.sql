-- =============================================================================
-- Salary history (append-only, effective-dated).
-- =============================================================================

CREATE TABLE salary_history (
    id             uuid           PRIMARY KEY DEFAULT uuidv7(),
    employee_id    uuid           NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    amount         numeric(18,4)  NOT NULL,
    currency_code  varchar(3)     NOT NULL DEFAULT 'INR',
    effective_from date           NOT NULL,
    effective_to   date           NULL,
    reason         text           NULL,
    created_at     timestamptz    NOT NULL DEFAULT now(),
    created_by     uuid           NULL,
    updated_at     timestamptz    NOT NULL DEFAULT now(),
    updated_by     uuid           NULL,
    deleted_at     timestamptz    NULL,
    version        bigint         NOT NULL DEFAULT 0,
    CONSTRAINT ck_salary_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_salary_currency_len CHECK (char_length(currency_code) = 3),
    CONSTRAINT ck_salary_date_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ix_salary_history_employee
    ON salary_history (employee_id, effective_from DESC)
    WHERE deleted_at IS NULL;
