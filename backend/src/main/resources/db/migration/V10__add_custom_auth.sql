-- Migrate Keycloak to Custom database-backed JWT Authentication
-- Drops Keycloak fields and creates local password storage and roles mapping

ALTER TABLE employee DROP COLUMN IF EXISTS keycloak_user_id;
ALTER TABLE employee ADD COLUMN password_hash varchar(255) NULL;

CREATE TABLE employee_role (
    employee_id uuid NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    role varchar(50) NOT NULL,
    PRIMARY KEY (employee_id, role)
);

-- Seed default admin@acme.local (password: Admin#12345)
INSERT INTO employee (id, employee_code, first_name, last_name, email, date_of_joining, employment_status, password_hash, tenant_id)
VALUES ('0190df0d-ffff-7c00-a000-000000000001', 'ADMIN-001', 'System', 'Administrator', 'admin@acme.local', '2026-01-01', 'ACTIVE', '$2a$10$7iRalX1HJb11yFesRIA0OOpxlCcgMZDTANyR/5X.WOoZzZ.8h4xri', '00000000-0000-0000-0000-000000000000'::uuid)
ON CONFLICT DO NOTHING;

INSERT INTO employee_role (employee_id, role)
VALUES ('0190df0d-ffff-7c00-a000-000000000001', 'SUPER_ADMIN'),
       ('0190df0d-ffff-7c00-a000-000000000001', 'HR_ADMIN'),
       ('0190df0d-ffff-7c00-a000-000000000001', 'FINANCE_ADMIN')
ON CONFLICT DO NOTHING;

-- Seed default hr@acme.local (password: Hr#1234567)
INSERT INTO employee (id, employee_code, first_name, last_name, email, date_of_joining, employment_status, password_hash, tenant_id)
VALUES ('0190df0d-ffff-7c00-a000-000000000002', 'HR-001', 'Hira', 'HR', 'hr@acme.local', '2026-01-01', 'ACTIVE', '$2a$10$aO6iSH.C8aJSra0OL3eLVuFIeMESrgpvKKbdG9r0mvLu7unTnUEnm', '00000000-0000-0000-0000-000000000000'::uuid)
ON CONFLICT DO NOTHING;

INSERT INTO employee_role (employee_id, role)
VALUES ('0190df0d-ffff-7c00-a000-000000000002', 'HR_ADMIN')
ON CONFLICT DO NOTHING;

-- Seed default employee@acme.local (password: Emp#1234567)
INSERT INTO employee (id, employee_code, first_name, last_name, email, date_of_joining, employment_status, password_hash, tenant_id)
VALUES ('0190df0d-ffff-7c00-a000-000000000003', 'EMP-001', 'Emp', 'Loyee', 'employee@acme.local', '2026-01-01', 'ACTIVE', '$2a$10$prerpAR3/lvMBRwHW.vsB.XyoapatKfcBw7TYJts5jVZoQeuaSFn6', '00000000-0000-0000-0000-000000000000'::uuid)
ON CONFLICT DO NOTHING;

INSERT INTO employee_role (employee_id, role)
VALUES ('0190df0d-ffff-7c00-a000-000000000003', 'EMPLOYEE')
ON CONFLICT DO NOTHING;
