# HRMS — RBAC Matrix

> This file is **authoritative**. Every API endpoint and UI screen must trace
> back to a row here. Changes to this matrix require an ADR.

## Roles & Scopes Hierarchy

Authorisation is calculated as `Permission = Resource + Action + Scope`.

| Role | Scope Intent & Key Responsibilities |
| :--- | :--- |
| `SUPER_ADMIN` | **GLOBAL**: System/SaaS platform owner. Tenant onboarding, global audit log, cross-company administration. |
| `ORGANIZATION_ADMIN` | **ORGANIZATION**: Company top administrator. Company settings, departments, locations, policy configuration. |
| `HR_ADMIN` | **ORGANIZATION**: Full HR operations, employee master data, salary management, appraisals, and policy runs. |
| `HR_EXECUTIVE` | **ORGANIZATION (Restricted)**: Operations HR (onboarding, leave processing, document uploads). Restricted from salary & org settings. |
| `FINANCE_ADMIN` | **ORGANIZATION**: Salary read/write, billing, invoice management, financial reports. |
| `PAYROLL_ADMIN` | **ORGANIZATION**: Dedicated payroll run execution, salary component setup, payslip generation. |
| `RECRUITER` | **ORGANIZATION**: Job requisitions, postings, candidate applications, and interview scheduling. |
| `LEADERSHIP` | **ORGANIZATION (Aggregated)**: Executive dashboards and high-level KPIs. **Never raw individual salary amounts.** |
| `PROJECT_MANAGER` | **TEAM / PROJECT**: Manages projects, clients, and team member allocations they own. |
| `EMPLOYEE` | **SELF**: Baseline self-service role (own profile, own leave, own payslips, own documents). |

> **Note on Manager Role**: `MANAGER` access is an inferred **Organizational Relationship** (`TEAM` scope) calculated dynamically from the `employee.manager_id` reporting tree. Any employee with direct or indirect reports inherits `TEAM`-scoped management permissions.

A user can hold multiple roles; effective permissions are the **union** of their scopes.

## Conventions & Scope Levels

- **`SELF`**: Evaluated where `target_employee_id == calling_user.id`.
- **`TEAM`**: Evaluated for any descendant in `employee.manager_id` tree or active project members on managed projects.
- **`DEPARTMENT`**: Evaluated for employees sharing the calling user's `department_id`.
- **`ORGANIZATION`**: Evaluated for any record belonging to the calling user's `tenant_id`.
- **`GLOBAL`**: Unrestricted cross-tenant platform access (`SUPER_ADMIN`).
- Empty cell = denied (HTTP 403).

## 1. Employee

| Action                              | SUPER | HR  | FIN | LEAD          | MGR     | PM             | EMP        |
|-------------------------------------|:-----:|:---:|:---:|:-------------:|:-------:|:--------------:|:----------:|
| List employees (no salary fields)   | All   | All | All | All           | Reports | Project members| Self only  |
| Read profile                        | All   | All | All | All           | Reports | Project members| Self       |
| Create employee                     | ✔     | ✔   |     |               |         |                |            |
| Edit employee — HR fields           | ✔     | ✔   |     |               |         |                |            |
| Edit employee — own contact info    | ✔     | ✔   |     |               |         |                | Self       |
| Terminate / mark inactive           | ✔     | ✔   |     |               |         |                |            |
| Soft-delete employee record         | ✔     | ✔   |     |               |         |                |            |
| Hard delete (audit-only)            | ✔     |     |     |               |         |                |            |

## 2. Department / Designation / Location

| Action            | SUPER | HR  | FIN | LEAD | MGR | PM  | EMP |
|-------------------|:-----:|:---:|:---:|:----:|:---:|:---:|:---:|
| Read              | ✔     | ✔   | ✔   | ✔    | ✔   | ✔   | ✔   |
| Create / Edit     | ✔     | ✔   |     |      |     |     |     |
| Soft delete       | ✔     | ✔   |     |      |     |     |     |

## 3. Salary

| Action                                   | SUPER | HR  | FIN | LEAD                | MGR | PM  | EMP        |
|------------------------------------------|:-----:|:---:|:---:|:-------------------:|:---:|:---:|:----------:|
| Read amount                              | ✔     | ✔   | ✔   | Aggregates only     |     |     | Self only  |
| Read salary history                      | ✔     | ✔   | ✔   |                     |     |     | Self only  |
| Write (new salary record)                | ✔     | ✔   | ✔   |                     |     |     |            |
| Issue correction (supersede prior row)   | ✔     | ✔   | ✔   |                     |     |     |            |
| Export salary report                     | ✔     | ✔   | ✔   |                     |     |     |            |

> Reads of `salary.amount` produce an `AUDIT_READ` row in `audit_log`.

## 4. Documents

| Action                                              | SUPER | HR  | FIN | LEAD | MGR | PM  | EMP                    |
|-----------------------------------------------------|:-----:|:---:|:---:|:----:|:---:|:---:|:----------------------:|
| Upload — public type (e.g. profile photo)           | ✔     | ✔   |     |      |     |     | Self                   |
| Upload — restricted type (offer letter, ID proof)   | ✔     | ✔   |     |      |     |     |                        |
| Download — own non-restricted documents             | ✔     | ✔   |     |      |     |     | Self                   |
| Download — restricted document (HR-only types)      | ✔     | ✔   |     |      |     |     | Self if marked sharable |
| Delete document                                     | ✔     | ✔   |     |      |     |     |                        |

## 5. Projects / Clients / Allocations

| Action                                | SUPER | HR  | FIN | LEAD          | MGR     | PM             | EMP                   |
|---------------------------------------|:-----:|:---:|:---:|:-------------:|:-------:|:--------------:|:---------------------:|
| List clients                          | ✔     | ✔   | ✔   | ✔             | ✔       | Own            |                       |
| Manage clients                        | ✔     |     | ✔   |               |         |                |                       |
| List projects                         | ✔     | ✔   | ✔   | ✔             | ✔       | Own + member of| Member of             |
| Create / edit project                 | ✔     |     | ✔   |               |         | Own (as PM)    |                       |
| List allocations                      | ✔     | ✔   | ✔   | ✔             | Reports | Own projects   | Self                  |
| Create / edit allocation              | ✔     | ✔   | ✔   |               |         | Own projects   |                       |
| Read project rate cards               | ✔     |     | ✔   | Aggregates only|        |                |                       |
| Edit project rate cards               | ✔     |     | ✔   |               |         |                |                       |

## 6. Billing

| Action                                | SUPER | HR  | FIN | LEAD            | MGR | PM            | EMP |
|---------------------------------------|:-----:|:---:|:---:|:---------------:|:---:|:-------------:|:---:|
| Read monthly billing — by project     | ✔     |     | ✔   | Aggregates only |     | Own projects  |     |
| Generate / regenerate billing         | ✔     |     | ✔   |                 |     |               |     |
| Export billing report                 | ✔     |     | ✔   |                 |     |               |     |

## 7. Appraisals

| Action                              | SUPER | HR  | FIN | LEAD | MGR              | PM  | EMP        |
|-------------------------------------|:-----:|:---:|:---:|:----:|:----------------:|:---:|:----------:|
| Manage appraisal cycles             | ✔     | ✔   |     |      |                  |     |            |
| Read appraisal — own                | ✔     | ✔   |     |      | Reports          |     | Self       |
| Submit reviewer ratings             | ✔     | ✔   |     |      | Reports          |     |            |
| Submit self-review                  | ✔     |     |     |      |                  |     | Self       |
| Read appraisal letter (document)    | ✔     | ✔   |     |      |                  |     | Self       |

## 8. Slack presence

| Action                          | SUPER | HR  | FIN | LEAD | MGR     | PM            | EMP |
|---------------------------------|:-----:|:---:|:---:|:----:|:-------:|:-------------:|:---:|
| View daily presence summary     | ✔     | ✔   |     | ✔    | Reports | Project members|    |
| View raw presence snapshots     | ✔     |     |     |      |         |                |    |

> Presence is informational only. It is **never** an input to salary,
> attendance, or appraisal.

## 9. Audit log

| Action                  | SUPER     | HR  | FIN | LEAD | MGR | PM  | EMP |
|-------------------------|:---------:|:---:|:---:|:----:|:---:|:---:|:---:|
| View audit log          | ✔         |     |     |      |     |     |     |
| Export audit log        | ✔         |     |     |      |     |     |     |

## 10. Dashboards

| Card / Section                    | SUPER | HR  | FIN | LEAD                 | MGR     | PM            | EMP |
|-----------------------------------|:-----:|:---:|:---:|:--------------------:|:-------:|:-------------:|:---:|
| Headcount, joiners, leavers       | ✔     | ✔   |     | ✔                    | Reports |               |     |
| Bench % / utilization             | ✔     |     | ✔   | ✔                    |         | Own projects  |     |
| Billing vs salary (aggregated)    | ✔     |     | ✔   | ✔                    |         |               |     |
| Project profitability             | ✔     |     | ✔   | ✔                    |         | Own projects  |     |
| Appraisals due                    | ✔     | ✔   |     | ✔                    | Reports |               |     |

## Test policy

For every cell in the matrix marked ✔ (or with a scope) there is at least one
integration test asserting **200**. For every empty cell there is at least one
test asserting **403**. Tests live in
`backend/src/test/java/com/acme/hrms/security/RbacMatrixTest.java`.

A red CI on `RbacMatrixTest` blocks merge. Period.
