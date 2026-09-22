-- =============================================================================
-- HRMS schema baseline.
--
-- This migration is *infrastructure-only*: it creates the cross-cutting
-- audit_log table (partitioned monthly per ADR-aligned architecture.md §3.5),
-- and a tiny helper to manage future partitions. Business tables land in V2+.
--
-- Conventions:
--   - All timestamps are timestamptz, stored in UTC.
--   - Primary keys are uuid DEFAULT uuidv7() (PG18 builtin).
--   - audit_log is APPEND-ONLY. No updates, no soft delete, no foreign keys
--     pointing to it. Writes are guaranteed to land via INSERT.
-- =============================================================================

-- Extensions are bootstrapped in infra/postgres/init/01-extensions.sql on
-- first container start. Repeating them here is idempotent and protects
-- non-Docker deployments.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

-- -----------------------------------------------------------------------------
-- Compatibility: uuidv7()
--
-- PG18+ ships uuidv7() in pg_catalog. Older versions do not, so we create a
-- same-signature helper in the current schema as a fallback to keep migrations
-- portable across local/dev/prod environments.
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF to_regprocedure('pg_catalog.uuidv7()') IS NULL THEN
        EXECUTE $fn$
            CREATE OR REPLACE FUNCTION uuidv7()
            RETURNS uuid
            LANGUAGE sql
            VOLATILE
            AS 'SELECT gen_random_uuid()'
        $fn$;
    END IF;
END
$$;

-- -----------------------------------------------------------------------------
-- audit_log
--
-- Append-only journal of every write (and every read of sensitive fields)
-- in the system. Partitioned by month on `at` so old partitions can be
-- detached / archived without rewrite; queries that filter by date range
-- benefit from partition pruning.
--
-- Columns:
--   id           uuidv7, primary key.
--   at           when the event happened (server time, UTC).
--   actor_id     keycloak subject UUID of the caller, or NULL for system jobs.
--   actor_label  human-friendly fallback (email/username) — useful when an
--                actor is later deleted from Keycloak.
--   action       enum-style code (CREATE, UPDATE, SOFT_DELETE, HARD_DELETE,
--                READ_SENSITIVE, LOGIN, EXPORT). See AuditAction.java.
--   entity       short name of the target table/aggregate (e.g. 'employee').
--   entity_id    primary key of the target row, if any.
--   request_id   X-Request-Id correlator, matches the response header and
--                MDC value, so a problem+json error and the audit row that
--                preceded it can be joined.
--   ip           caller IP address; optional.
--   before_json  serialized state before the change (for UPDATE/DELETE).
--   after_json   serialized state after the change (for CREATE/UPDATE).
--   detail       optional free-text note (e.g. "salary corrected for typo").
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id           uuid        NOT NULL DEFAULT uuidv7(),
    at           timestamptz NOT NULL DEFAULT now(),
    actor_id     uuid        NULL,
    actor_label  text        NULL,
    action       text        NOT NULL,
    entity       text        NOT NULL,
    entity_id    uuid        NULL,
    request_id   text        NULL,
    ip           inet        NULL,
    before_json  jsonb       NULL,
    after_json   jsonb       NULL,
    detail       text        NULL,
    -- Composite PK because Postgres requires the partition key in the PK
    -- of a partitioned table.
    CONSTRAINT pk_audit_log PRIMARY KEY (id, at),
    CONSTRAINT ck_audit_log_action CHECK (
        action IN ('CREATE','UPDATE','SOFT_DELETE','HARD_DELETE','READ_SENSITIVE','LOGIN','EXPORT')
    )
)
PARTITION BY RANGE (at);

-- Indexes on the parent are inherited by every partition.
CREATE INDEX IF NOT EXISTS ix_audit_log_actor       ON audit_log (actor_id, at DESC);
CREATE INDEX IF NOT EXISTS ix_audit_log_entity      ON audit_log (entity, entity_id, at DESC);
CREATE INDEX IF NOT EXISTS ix_audit_log_action      ON audit_log (action, at DESC);
CREATE INDEX IF NOT EXISTS ix_audit_log_request_id  ON audit_log (request_id) WHERE request_id IS NOT NULL;

-- -----------------------------------------------------------------------------
-- Helper: ensure_audit_log_partition(month_start date)
-- Creates `audit_log_yyyy_mm` for the given month if it does not exist.
-- Used by the migration below to seed partitions, and intended for future
-- use by a Spring scheduler that rolls partitions forward.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION ensure_audit_log_partition(month_start date)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    part_name text := format('audit_log_%s', to_char(month_start, 'YYYY_MM'));
    range_from text := to_char(month_start, 'YYYY-MM-DD');
    range_to   text := to_char(month_start + INTERVAL '1 month', 'YYYY-MM-DD');
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_log FOR VALUES FROM (%L) TO (%L)',
        part_name, range_from, range_to
    );
END;
$$;

-- Seed 14 partitions: previous month + current + 12 future months. This
-- gives ~13 months of runway before the scheduler needs to add new ones.
DO $$
DECLARE
    base_month date := date_trunc('month', now())::date;
    i int;
BEGIN
    FOR i IN -1..12 LOOP
        PERFORM ensure_audit_log_partition((base_month + (i || ' months')::interval)::date);
    END LOOP;
END $$;
