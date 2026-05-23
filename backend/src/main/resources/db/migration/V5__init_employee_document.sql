-- =============================================================================
-- Employee documents: metadata in Postgres, blobs in S3-compatible storage.
-- =============================================================================

CREATE TABLE employee_document (
    id                 uuid        PRIMARY KEY DEFAULT uuidv7(),
    employee_id        uuid        NOT NULL REFERENCES employee(id) ON DELETE RESTRICT,
    document_type      text        NOT NULL,
    restricted         boolean     NOT NULL,
    sharable           boolean     NOT NULL DEFAULT false,
    storage_key        text        NOT NULL,
    original_filename  text        NOT NULL,
    content_type       text        NOT NULL,
    size_bytes         bigint      NULL,
    upload_status      text        NOT NULL DEFAULT 'PENDING',
    created_at         timestamptz NOT NULL DEFAULT now(),
    created_by         uuid        NULL,
    updated_at         timestamptz NOT NULL DEFAULT now(),
    updated_by         uuid        NULL,
    deleted_at         timestamptz NULL,
    version            bigint      NOT NULL DEFAULT 0,
    CONSTRAINT ck_employee_document_type CHECK (
        document_type IN ('PROFILE_PHOTO', 'OFFER_LETTER', 'ID_PROOF', 'OTHER')
    ),
    CONSTRAINT ck_employee_document_upload_status CHECK (
        upload_status IN ('PENDING', 'UPLOADED')
    ),
    CONSTRAINT ck_employee_document_filename_nonempty CHECK (length(original_filename) > 0),
    CONSTRAINT ck_employee_document_content_type_nonempty CHECK (length(content_type) > 0),
    CONSTRAINT ck_employee_document_storage_key_nonempty CHECK (length(storage_key) > 0)
);

CREATE INDEX ix_employee_document_employee
    ON employee_document (employee_id) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ux_employee_document_storage_key_live
    ON employee_document (storage_key) WHERE deleted_at IS NULL;
