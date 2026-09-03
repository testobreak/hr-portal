-- HRMS PostgreSQL bootstrap.
--
-- This runs only on first container start (empty data dir). Idempotent.
-- Application schema lives in Flyway migrations under
-- backend/src/main/resources/db/migration. Do NOT add tables here.

-- pgcrypto: gen_random_bytes() for tokens, crypt() if ever needed.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- citext: case-insensitive text for emails, slugs.
CREATE EXTENSION IF NOT EXISTS citext;

-- Compatibility: uuidv7()
-- Built into PG18+, for PG 16/17 fallback to random uuid if not available.
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
END $$;

-- Sanity check: fail loudly if someone tries to run this on PG < 16.
DO $$
BEGIN
  IF current_setting('server_version_num')::int < 160000 THEN
    RAISE EXCEPTION 'HRMS requires PostgreSQL 16+; running %', version();
  END IF;
END $$;
