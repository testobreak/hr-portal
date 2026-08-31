-- Flyway Migration V11: Expand granular RBAC roles (ORGANIZATION_ADMIN, HR_EXECUTIVE, PAYROLL_ADMIN, RECRUITER)
-- Establishes scope-based permission structure and assigns expanded roles to seed users

-- Assign ORGANIZATION_ADMIN role to admin@acme.local (ID: 0190df0d-ffff-7c00-a000-000000000001)
INSERT INTO employee_role (employee_id, role)
VALUES ('0190df0d-ffff-7c00-a000-000000000001', 'ORGANIZATION_ADMIN')
ON CONFLICT DO NOTHING;

-- Assign HR_EXECUTIVE role to hr@acme.local (ID: 0190df0d-ffff-7c00-a000-000000000002)
INSERT INTO employee_role (employee_id, role)
VALUES ('0190df0d-ffff-7c00-a000-000000000002', 'HR_EXECUTIVE')
ON CONFLICT DO NOTHING;
