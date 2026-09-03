-- =============================================================================
-- HRMS Development Seed Data
--
-- This script populates sample master data for local development and UI testing.
-- All records belong to the default tenant: 00000000-0000-0000-0000-000000000000
-- =============================================================================

DO $$
DECLARE
    v_tenant_id uuid := '00000000-0000-0000-0000-000000000000'::uuid;
    v_entity_id uuid;
    v_dept_eng uuid;
    v_dept_hr uuid;
    v_dept_fin uuid;
    v_desig_se uuid;
    v_desig_lead uuid;
    v_desig_hr uuid;
    v_loc_hq uuid;
    v_loc_remote uuid;
    v_client_acme uuid;
    v_client_globex uuid;
    v_proj_core uuid;
    v_proj_mobile uuid;
    v_admin_id uuid := '0190df0d-ffff-7c00-a000-000000000001'::uuid;
    v_hr_id uuid := '0190df0d-ffff-7c00-a000-000000000002'::uuid;
    v_emp_id uuid := '0190df0d-ffff-7c00-a000-000000000003'::uuid;
BEGIN
    -- 1. Legal Entity
    INSERT INTO legal_entity (id, tenant_id, code, name)
    VALUES ('0190df0e-0001-7000-a000-000000000001'::uuid, v_tenant_id, 'ACME-CORP', 'Acme Corporation Inc.')
    ON CONFLICT DO NOTHING
    RETURNING id INTO v_entity_id;

    IF v_entity_id IS NULL THEN
        SELECT id INTO v_entity_id FROM legal_entity WHERE tenant_id = v_tenant_id AND code = 'ACME-CORP';
    END IF;

    -- 2. Locations
    INSERT INTO location (id, tenant_id, code, name, city, country)
    VALUES 
        ('0190df0e-0002-7000-a000-000000000001'::uuid, v_tenant_id, 'HQ-BLR', 'Bangalore Global Tech Park', 'Bangalore', 'India'),
        ('0190df0e-0002-7000-a000-000000000002'::uuid, v_tenant_id, 'REMOTE-IND', 'India Remote Workforce', 'Remote', 'India')
    ON CONFLICT DO NOTHING;

    SELECT id INTO v_loc_hq FROM location WHERE tenant_id = v_tenant_id AND code = 'HQ-BLR';
    SELECT id INTO v_loc_remote FROM location WHERE tenant_id = v_tenant_id AND code = 'REMOTE-IND';

    -- 3. Departments
    INSERT INTO department (id, tenant_id, code, name, description)
    VALUES 
        ('0190df0e-0003-7000-a000-000000000001'::uuid, v_tenant_id, 'ENG', 'Engineering & Technology', 'Software development and infrastructure'),
        ('0190df0e-0003-7000-a000-000000000002'::uuid, v_tenant_id, 'HR', 'Human Resources & Talent', 'People operations and recruiting'),
        ('0190df0e-0003-7000-a000-000000000003'::uuid, v_tenant_id, 'FIN', 'Finance & Accounting', 'Payroll, billing and corporate finance')
    ON CONFLICT DO NOTHING;

    SELECT id INTO v_dept_eng FROM department WHERE tenant_id = v_tenant_id AND code = 'ENG';
    SELECT id INTO v_dept_hr FROM department WHERE tenant_id = v_tenant_id AND code = 'HR';
    SELECT id INTO v_dept_fin FROM department WHERE tenant_id = v_tenant_id AND code = 'FIN';

    -- 4. Designations
    INSERT INTO designation (id, tenant_id, title, level, description)
    VALUES 
        ('0190df0e-0004-7000-a000-000000000001'::uuid, v_tenant_id, 'Software Engineer', 'L2', 'Core fullstack developer'),
        ('0190df0e-0004-7000-a000-000000000002'::uuid, v_tenant_id, 'Engineering Lead', 'L4', 'Technical lead and team mentor'),
        ('0190df0e-0004-7000-a000-000000000003'::uuid, v_tenant_id, 'HR Business Partner', 'L3', 'People ops and performance')
    ON CONFLICT DO NOTHING;

    SELECT id INTO v_desig_se FROM designation WHERE tenant_id = v_tenant_id AND title = 'Software Engineer';
    SELECT id INTO v_desig_lead FROM designation WHERE tenant_id = v_tenant_id AND title = 'Engineering Lead';
    SELECT id INTO v_desig_hr FROM designation WHERE tenant_id = v_tenant_id AND title = 'HR Business Partner';

    -- Update seeded employee records with department, designation, location, legal entity
    UPDATE employee 
       SET department_id = v_dept_eng,
           designation_id = v_desig_lead,
           location_id = v_loc_hq,
           legal_entity_id = v_entity_id
     WHERE id = v_admin_id AND department_id IS NULL;

    UPDATE employee 
       SET department_id = v_dept_hr,
           designation_id = v_desig_hr,
           location_id = v_loc_hq,
           legal_entity_id = v_entity_id,
           manager_id = v_admin_id
     WHERE id = v_hr_id AND department_id IS NULL;

    UPDATE employee 
       SET department_id = v_dept_eng,
           designation_id = v_desig_se,
           location_id = v_loc_remote,
           legal_entity_id = v_entity_id,
           manager_id = v_admin_id
     WHERE id = v_emp_id AND department_id IS NULL;

    -- 5. Clients
    INSERT INTO client (id, tenant_id, code, name, description)
    VALUES 
        ('0190df0e-0005-7000-a000-000000000001'::uuid, v_tenant_id, 'CL-ACME', 'Acme Retail Solutions', 'Enterprise omnichannel commerce'),
        ('0190df0e-0005-7000-a000-000000000002'::uuid, v_tenant_id, 'CL-GLOBEX', 'Globex International', 'Fintech core platform modernization')
    ON CONFLICT DO NOTHING;

    SELECT id INTO v_client_acme FROM client WHERE tenant_id = v_tenant_id AND code = 'CL-ACME';
    SELECT id INTO v_client_globex FROM client WHERE tenant_id = v_tenant_id AND code = 'CL-GLOBEX';

    -- 6. Projects
    INSERT INTO project (id, tenant_id, client_id, project_code, name, description, project_manager_id, status, start_date)
    VALUES 
        ('0190df0e-0006-7000-a000-000000000001'::uuid, v_tenant_id, v_client_acme, 'PRJ-PORTAL', 'HR Portal Modernization', 'Internal HR & Billing platform', v_admin_id, 'ACTIVE', '2026-01-01'),
        ('0190df0e-0006-7000-a000-000000000002'::uuid, v_tenant_id, v_client_globex, 'PRJ-PAYMENTS', 'Globex NextGen Checkout', 'High throughput payment gateway', v_admin_id, 'ACTIVE', '2026-02-01')
    ON CONFLICT DO NOTHING;

    SELECT id INTO v_proj_core FROM project WHERE tenant_id = v_tenant_id AND project_code = 'PRJ-PORTAL';
    SELECT id INTO v_proj_mobile FROM project WHERE tenant_id = v_tenant_id AND project_code = 'PRJ-PAYMENTS';

    -- 7. Allocations
    INSERT INTO allocation (id, tenant_id, project_id, employee_id, allocation_percentage, role_title, start_date)
    VALUES 
        ('0190df0e-0007-7000-a000-000000000001'::uuid, v_tenant_id, v_proj_core, v_emp_id, 80, 'Fullstack Engineer', '2026-01-01'),
        ('0190df0e-0007-7000-a000-000000000002'::uuid, v_tenant_id, v_proj_mobile, v_emp_id, 20, 'Backend Consultant', '2026-02-01')
    ON CONFLICT DO NOTHING;

    -- 8. Leave Types
    INSERT INTO leave_type (id, tenant_id, code, name, category, default_days_per_year, paid)
    VALUES 
        ('0190df0e-0008-7000-a000-000000000001'::uuid, v_tenant_id, 'AL', 'Annual Leave', 'ANNUAL', 18, true),
        ('0190df0e-0008-7000-a000-000000000002'::uuid, v_tenant_id, 'SL', 'Sick Leave', 'SICK', 12, true),
        ('0190df0e-0008-7000-a000-000000000003'::uuid, v_tenant_id, 'CO', 'Compensatory Off', 'COMPENSATORY', 0, true)
    ON CONFLICT DO NOTHING;

    -- 9. Holiday Calendar & Holidays
    INSERT INTO holiday_calendar (id, tenant_id, code, name, year, location_id)
    VALUES ('0190df0e-0009-7000-a000-000000000001'::uuid, v_tenant_id, 'IND-2026', 'India General Holidays 2026', 2026, v_loc_hq)
    ON CONFLICT DO NOTHING;

    INSERT INTO holiday (id, tenant_id, calendar_id, name, holiday_date, optional)
    VALUES 
        ('0190df0e-0010-7000-a000-000000000001'::uuid, v_tenant_id, '0190df0e-0009-7000-a000-000000000001'::uuid, 'Republic Day', '2026-01-26', false),
        ('0190df0e-0010-7000-a000-000000000002'::uuid, v_tenant_id, '0190df0e-0009-7000-a000-000000000001'::uuid, 'Independence Day', '2026-08-15', false),
        ('0190df0e-0010-7000-a000-000000000003'::uuid, v_tenant_id, '0190df0e-0009-7000-a000-000000000001'::uuid, 'Gandhi Jayanti', '2026-10-02', false)
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'Dev master data seeded successfully!';
END $$;
