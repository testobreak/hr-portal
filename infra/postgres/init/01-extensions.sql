-- HRMS PostgreSQL bootstrap.
--
-- This runs only on first container start (empty data dir). Idempotent.
-- Application schema lives in Flyway migrations under
-- backend/src/main/resources/db/migration. Do NOT add tables here.

-- pgcrypto: gen_random_bytes() for tokens, crypt() if ever needed.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- citext: case-insensitive text for emails, slugs.
CREATE EXTENSION IF NOT EXISTS citext;

-- Note on UUIDv7: PostgreSQL 18 ships uuidv7() as a built-in function.
-- We use it directly in DEFAULT clauses; no extension required.
--   id uuid PRIMARY KEY DEFAULT uuidv7()

-- Sanity check: fail loudly if someone tries to run this on PG < 18.
DO $$
BEGIN
  IF current_setting('server_version_num')::int < 180000 THEN
    RAISE EXCEPTION 'HRMS requires PostgreSQL 18+; running %', version();
  END IF;
END $$;
