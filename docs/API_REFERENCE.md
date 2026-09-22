# ACME HRMS REST API Reference Manual

> **Platform**: Acme Enterprise HRMS & Resource Management Platform  
> **API Version**: 1.0 (OpenAPI 3.1.0) | **Total Endpoints**: 135 | **Total Operations**: 177  
> **Specification Source**: Real-time inspection of Spring Boot backend controllers & JPA entities  

---

## 1. Architectural & Integration Standards

### 1.1 Base URLs & Environments

The API gateway routes requests to the HRMS service mesh according to deployment tier:

| Environment | Base URL | Network / Security Tier | Usage / Purpose |
| :--- | :--- | :--- | :--- |
| **Production (Public Gateway)** | `https://api.hrms.acme.com` | Public Ingress / Cloudflare WAF / TLS 1.3 | Web client & mobile production traffic |
| **Production (Internal VPC / VPN)** | `https://hrms.internal.acme.corp/api` | Zero-Trust Private Network / AWS ALB | Corporate intranet, internal cron workers, HR tools |
| **Staging / UAT** | `https://staging-api.hrms.acme.com` | Restricted VPN / Corporate SSO Gateway | Pre-release QA, automated E2E tests, stakeholder review |
| **Development / Sandbox** | `https://dev-api.hrms.acme.com` | Internal Dev Gateway / Sandbox Keycloak | CI/CD pipeline builds, feature branch integration |
| **Local Development (Direct)** | `http://localhost:8080` | Local workstation loopback | Direct Spring Boot backend testing (Swagger, Postman, curl) |
| **Local Development (Vite Proxy)** | `http://localhost:5173/api` | Local frontend dev proxy | Web UI pairing with automatic CORS proxying |
| **Kubernetes / Docker Network** | `http://hrms-backend.hrms.svc.cluster.local:8080` | Cluster Internal CoreDNS (`svc.cluster.local`) | East-West microservice communication & background jobs |

#### Environment Configuration Variables
Client applications configure the target base URL using standard environment variables:
- **Frontend (Vite / React)**: `VITE_API_BASE_URL=https://api.hrms.acme.com` (defaults to `/api` proxy in local dev)
- **Backend / Microservices**: `HRMS_API_BASE_URL=https://api.hrms.acme.com`
- **CORS Allowed Origins**: Configured via `HRMS_CORS_ALLOWED_ORIGINS` (e.g. `https://hrms.acme.com,https://app.hrms.acme.com`)


### 1.2 Authentication Scheme
All endpoints, except public guest recruiting endpoints (`/api/v1/careers/**`) and login (`/api/v1/auth/login`), require an RFC 7519 JSON Web Token (JWT) passed in the `Authorization` HTTP header:
```http
Authorization: Bearer <access_token>
```
- **Token Issuer**: `hrms-backend` / Keycloak Realm
- **Signing Algorithm**: `HS256` / `RS256`
- **JWT Claims**: `sub` (Employee UUID), `email`, `roles` (Array of assigned RBAC strings), `tenant_id`

### 1.3 Role-Based Access Control (RBAC) Matrix
| Role Identifier | Scope & Privileges |
| :--- | :--- |
| `SUPER_ADMIN` | Root system administrator: tenant configuration, global audit log review, hard deletion |
| `ORGANIZATION_ADMIN` | Organization master data: legal entities, departments, locations, policy defaults |
| `HR_ADMIN` | Complete employee lifecycle: hire, onboarding, employee profiles, requisitions, salary records |
| `FINANCE_ADMIN` | Compensation, gross-to-net payroll runs, general ledger CSV and bank disbursement exports |
| `LEADERSHIP` | Executive analytics, department-wide headcount & billable utilization dashboards |
| `MANAGER` | Direct reports timesheet & leave approvals, team roster inspection, delegation of authority |
| `PROJECT_MANAGER` | Project allocations, client management, project-level timesheet reviews |
| `EMPLOYEE` | Self-service attendance punching, weekly timesheets, leave requests, personal profile, document downloads |
| `CANDIDATE` | Pre-hire portal onboarding tasks, document uploads, digital offer letter acceptance/rejection |
| `PUBLIC / Anonymous` | Careers board job browsing, guest resume application submission, offer preview |

### 1.4 Error Response Standard (RFC 7807 Problem Details)
When an endpoint produces a client error (4xx) or server error (5xx), the response body is formatted as an RFC 7807 `application/problem+json` object:
```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Employee with id 0190df0d-ffff-7c00-a000-000000000001 not found",
  "instance": "/api/employees/0190df0d-ffff-7c00-a000-000000000001"
}
```

### 1.5 Pagination, Sorting & Filtering Conventions
Collection endpoints accept standard Spring Data parameters:
- `page` *(integer, 0-indexed, default `0`)*: Current page number.
- `size` *(integer, default `10` or `20`)*: Number of items per page.
- `sort` *(string, default `createdAt,desc` or `firstName,asc`)*: Sort property and direction.
- `q` *(string, optional)*: Full-text search or prefix search filter.

---

## 2. API Modules Table of Contents

- [**01. Authentication & Session**](#01-authentication--session) (2 operations)
- [**02. Employee Master & Organization Setup**](#02-employee-master--organization-setup) (27 operations)
- [**03. Employee Directory**](#03-employee-directory) (2 operations)
- [**03. Employee Profiles & Change Requests**](#03-employee-profiles--change-requests) (8 operations)
- [**04. Projects, Clients & Allocations**](#04-projects-clients--allocations) (15 operations)
- [**05. Time, Attendance & Timesheets**](#05-time-attendance--timesheets) (18 operations)
- [**06. Leave & Absence Management**](#06-leave--absence-management) (8 operations)
- [**07. Compensation & Salary History**](#07-compensation--salary-history) (3 operations)
- [**07. Payroll Engine & Gross-to-Net**](#07-payroll-engine--gross-to-net) (12 operations)
- [**08. Recruitment - Candidate Applications**](#08-recruitment-candidate-applications) (10 operations)
- [**08. Recruitment - Internal Job Postings**](#08-recruitment-internal-job-postings) (4 operations)
- [**08. Recruitment - Interviews & Scorecards**](#08-recruitment-interviews--scorecards) (4 operations)
- [**08. Recruitment - Job Offers**](#08-recruitment-job-offers) (3 operations)
- [**08. Recruitment - Job Requisitions**](#08-recruitment-job-requisitions) (8 operations)
- [**08. Recruitment - Public Careers & Guest Portal**](#08-recruitment-public-careers--guest-portal) (7 operations)
- [**08. Recruitment - Vacancies & Openings**](#08-recruitment-vacancies--openings) (3 operations)
- [**09. Pre-Hire Onboarding & Candidate Activation**](#09-pre-hire-onboarding--candidate-activation) (12 operations)
- [**10. Document Management & MinIO S3**](#10-document-management--minio-s3) (13 operations)
- [**11. Company Announcements & Broadcasts**](#11-company-announcements--broadcasts) (4 operations)
- [**12. Workflows & Multi-Tier Approvals**](#12-workflows--multi-tier-approvals) (7 operations)
- [**13. Manager Self-Service & Hierarchy**](#13-manager-self-service--hierarchy) (4 operations)
- [**14. System Audit Logging**](#14-system-audit-logging) (1 operations)
- [**15. Executive Dashboard & Metrics**](#15-executive-dashboard--metrics) (2 operations)

---

## 01. Authentication & Session

Contains **2** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/me` | Return the identity and realm roles of the caller | `Authenticated` |
| `POST` | `/api/v1/auth/login` | Authenticate user and return a signed JWT token | `Public / Anonymous` |

### GET `/api/me`
**Purpose**: Return the identity and realm roles of the caller  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `MeResponse` |

---

### POST `/api/v1/auth/login`
**Purpose**: Authenticate user and return a signed JWT token  
**Access Control**: `Public / Anonymous`  

#### Request Body (`application/json`)
**DTO Model**: `LoginRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `email` | `string (email)` | **Yes** | - |
| `password` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LoginResponse` |

---

## 02. Employee Master & Organization Setup

Contains **27** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/departments` | List departments | `Authenticated (Any Role)` |
| `POST` | `/api/departments` | Create a department | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/departments/{id}` | Read a department | `Authenticated (Any Role)` |
| `PUT` | `/api/departments/{id}` | Update a department | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `DELETE` | `/api/departments/{id}` | Soft-delete a department | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/designations` | List all job designations | `Authenticated (Any Role)` |
| `POST` | `/api/designations` | Create a designation | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/designations/{id}` | Get job designation by ID | `Authenticated (Any Role)` |
| `PUT` | `/api/designations/{id}` | Update a designation | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `DELETE` | `/api/designations/{id}` | Soft-delete a designation | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/employees` | List employees within the caller's scope | `Authenticated (Scope-based: ALL / REPORTS / SELF)` |
| `POST` | `/api/employees` | Create an employee (HR / SUPER only) | `SUPER_ADMIN, HR_ADMIN` |
| `GET` | `/api/employees/{id}` | Read a single employee (within scope, with field redaction) | `Authenticated (Scope-based: ALL / REPORTS / SELF)` |
| `PUT` | `/api/employees/{id}` | Update an employee's HR fields (HR / SUPER only) | `SUPER_ADMIN, HR_ADMIN` |
| `DELETE` | `/api/employees/{id}` | Soft-delete an employee record | `SUPER_ADMIN, HR_ADMIN` |
| `PATCH` | `/api/employees/{id}/contact` | Update contact info (self for EMPLOYEE, anyone for HR/SUPER) | `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN` |
| `DELETE` | `/api/employees/{id}/permanent` | Hard-delete an employee record (SUPER_ADMIN only). Reserved for legal / compliance erasure; audited as HARD_DELETE. | `SUPER_ADMIN` |
| `GET` | `/api/legal-entities` | List legal entities | `Authenticated (Any Role)` |
| `POST` | `/api/legal-entities` | Create a legal entity | `SUPER_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/legal-entities/{id}` | Read a legal entity | `Authenticated (Any Role)` |
| `PUT` | `/api/legal-entities/{id}` | Update a legal entity | `SUPER_ADMIN, ORGANIZATION_ADMIN` |
| `DELETE` | `/api/legal-entities/{id}` | Soft-delete a legal entity | `SUPER_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/locations` | List all office locations | `Authenticated (Any Role)` |
| `POST` | `/api/locations` | Create a location | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `GET` | `/api/locations/{id}` | Get office location by ID | `Authenticated (Any Role)` |
| `PUT` | `/api/locations/{id}` | Update a location | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |
| `DELETE` | `/api/locations/{id}` | Soft-delete a location | `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN` |

### GET `/api/departments`
**Purpose**: List departments  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageDepartmentResponse` |

---

### POST `/api/departments`
**Purpose**: Create a department  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `DepartmentCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | - |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DepartmentResponse` |

---

### GET `/api/departments/{id}`
**Purpose**: Read a department  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DepartmentResponse` |

---

### PUT `/api/departments/{id}`
**Purpose**: Update a department  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `DepartmentUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DepartmentResponse` |

---

### DELETE `/api/departments/{id}`
**Purpose**: Soft-delete a department  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/designations`
**Purpose**: List all job designations  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageDesignationResponse` |

---

### POST `/api/designations`
**Purpose**: Create a designation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `DesignationCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `title` | `string` | **Yes** | - |
| `level` | `string` | No | - |
| `description` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DesignationResponse` |

---

### GET `/api/designations/{id}`
**Purpose**: Get job designation by ID  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DesignationResponse` |

---

### PUT `/api/designations/{id}`
**Purpose**: Update a designation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `DesignationUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `title` | `string` | **Yes** | - |
| `level` | `string` | No | - |
| `description` | `string` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DesignationResponse` |

---

### DELETE `/api/designations/{id}`
**Purpose**: Soft-delete a designation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/employees`
**Purpose**: List employees within the caller's scope  
**Access Control**: `Authenticated (Scope-based: ALL / REPORTS / SELF)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageEmployeeSummary` |

---

### POST `/api/employees`
**Purpose**: Create an employee (HR / SUPER only)  
**Access Control**: `SUPER_ADMIN, HR_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `EmployeeCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `employeeCode` | `string` | **Yes** | - |
| `firstName` | `string` | **Yes** | - |
| `lastName` | `string` | **Yes** | - |
| `email` | `string (email)` | **Yes** | - |
| `phoneNumber` | `string` | No | - |
| `dateOfBirth` | `string (date)` | No | - |
| `dateOfJoining` | `string (date)` | **Yes** | - |
| `employmentStatus` | `string` | No | - |
| `keycloakUserId` | `string (uuid)` | No | - |
| `departmentId` | `string (uuid)` | No | - |
| `designationId` | `string (uuid)` | No | - |
| `locationId` | `string (uuid)` | No | - |
| `legalEntityId` | `string (uuid)` | No | - |
| `managerId` | `string (uuid)` | No | - |
| `roles` | `Array<string>` | No | - |
| `keycloakPassword` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeResponse` |

---

### GET `/api/employees/{id}`
**Purpose**: Read a single employee (within scope, with field redaction)  
**Access Control**: `Authenticated (Scope-based: ALL / REPORTS / SELF)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeResponse` |

---

### PUT `/api/employees/{id}`
**Purpose**: Update an employee's HR fields (HR / SUPER only)  
**Access Control**: `SUPER_ADMIN, HR_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `EmployeeUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `firstName` | `string` | **Yes** | - |
| `lastName` | `string` | **Yes** | - |
| `email` | `string (email)` | **Yes** | - |
| `phoneNumber` | `string` | No | - |
| `dateOfBirth` | `string (date)` | No | - |
| `dateOfJoining` | `string (date)` | **Yes** | - |
| `employmentStatus` | `string` | **Yes** | - |
| `keycloakUserId` | `string (uuid)` | No | - |
| `departmentId` | `string (uuid)` | No | - |
| `designationId` | `string (uuid)` | No | - |
| `locationId` | `string (uuid)` | No | - |
| `legalEntityId` | `string (uuid)` | No | - |
| `managerId` | `string (uuid)` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeResponse` |

---

### DELETE `/api/employees/{id}`
**Purpose**: Soft-delete an employee record  
**Access Control**: `SUPER_ADMIN, HR_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### PATCH `/api/employees/{id}/contact`
**Purpose**: Update contact info (self for EMPLOYEE, anyone for HR/SUPER)  
**Access Control**: `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `EmployeeContactUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `phoneNumber` | `string` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeResponse` |

---

### DELETE `/api/employees/{id}/permanent`
**Purpose**: Hard-delete an employee record (SUPER_ADMIN only). Reserved for legal / compliance erasure; audited as HARD_DELETE.  
**Access Control**: `SUPER_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/legal-entities`
**Purpose**: List legal entities  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageLegalEntityResponse` |

---

### POST `/api/legal-entities`
**Purpose**: Create a legal entity  
**Access Control**: `SUPER_ADMIN, ORGANIZATION_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `LegalEntityCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | - |
| `name` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LegalEntityResponse` |

---

### GET `/api/legal-entities/{id}`
**Purpose**: Read a legal entity  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LegalEntityResponse` |

---

### PUT `/api/legal-entities/{id}`
**Purpose**: Update a legal entity  
**Access Control**: `SUPER_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `LegalEntityUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LegalEntityResponse` |

---

### DELETE `/api/legal-entities/{id}`
**Purpose**: Soft-delete a legal entity  
**Access Control**: `SUPER_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/locations`
**Purpose**: List all office locations  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageLocationResponse` |

---

### POST `/api/locations`
**Purpose**: Create a location  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `LocationCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | - |
| `name` | `string` | **Yes** | - |
| `city` | `string` | No | - |
| `country` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LocationResponse` |

---

### GET `/api/locations/{id}`
**Purpose**: Get office location by ID  
**Access Control**: `Authenticated (Any Role)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LocationResponse` |

---

### PUT `/api/locations/{id}`
**Purpose**: Update a location  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `LocationUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | - |
| `city` | `string` | No | - |
| `country` | `string` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LocationResponse` |

---

### DELETE `/api/locations/{id}`
**Purpose**: Soft-delete a location  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, ORGANIZATION_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 03. Employee Directory

Contains **2** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/directory/employees` | Query public employee directory cards and departments | `Authenticated` |
| `GET` | `/api/v1/directory/employees/{employeeId}` | Get public directory profile details | `Authenticated` |

### GET `/api/v1/directory/employees`
**Purpose**: Query public employee directory cards and departments  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `page` | `query` | `integer (int32)` | No | - |
| `size` | `query` | `integer (int32)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageDirectoryEmployeeResponse` |

---

### GET `/api/v1/directory/employees/{employeeId}`
**Purpose**: Get public directory profile details  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DirectoryEmployeeResponse` |

---

## 03. Employee Profiles & Change Requests

Contains **8** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/me/profile` | Fetch full personal profile, contact info, and employment metadata | `Authenticated` |
| `PATCH` | `/api/v1/me/profile` | Update self-service profile fields (phone, emergency contact) | `Authenticated` |
| `GET` | `/api/v1/me/profile-change-requests` | List pending profile change requests for HR review | `Authenticated` |
| `POST` | `/api/v1/me/profile-change-requests` | Submit change request for restricted profile fields | `Authenticated` |
| `GET` | `/api/v1/profile-change-requests/{requestId}` | getChangeRequest | `Authenticated` |
| `POST` | `/api/v1/profile-change-requests/{requestId}/approve` | approveChangeRequest | `Authenticated` |
| `POST` | `/api/v1/profile-change-requests/{requestId}/cancel` | cancelChangeRequest | `Authenticated` |
| `POST` | `/api/v1/profile-change-requests/{requestId}/reject` | rejectChangeRequest | `Authenticated` |

### GET `/api/v1/me/profile`
**Purpose**: Fetch full personal profile, contact info, and employment metadata  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProfileResponse` |

---

### PATCH `/api/v1/me/profile`
**Purpose**: Update self-service profile fields (phone, emergency contact)  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
`object`

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/me/profile-change-requests`
**Purpose**: List pending profile change requests for HR review  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/me/profile-change-requests`
**Purpose**: Submit change request for restricted profile fields  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
`object`

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProfileChangeRequestResponse` |

---

### GET `/api/v1/profile-change-requests/{requestId}`
**Purpose**: getChangeRequest  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProfileChangeRequestResponse` |

---

### POST `/api/v1/profile-change-requests/{requestId}/approve`
**Purpose**: approveChangeRequest  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/profile-change-requests/{requestId}/cancel`
**Purpose**: cancelChangeRequest  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/profile-change-requests/{requestId}/reject`
**Purpose**: rejectChangeRequest  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 04. Projects, Clients & Allocations

Contains **15** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/allocations` | List allocations within the caller's scope | `Authenticated (Scope-based)` |
| `POST` | `/api/allocations` | Create an allocation | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `GET` | `/api/allocations/{id}` | Read an allocation within the caller's scope | `Authenticated (Scope-based)` |
| `PUT` | `/api/allocations/{id}` | Update an allocation | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `DELETE` | `/api/allocations/{id}` | Soft-delete an allocation | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `GET` | `/api/clients` | List clients within the caller's scope | `Authenticated (Scope-based)` |
| `POST` | `/api/clients` | Create a client | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `GET` | `/api/clients/{id}` | Read a client within the caller's scope | `Authenticated (Scope-based)` |
| `PUT` | `/api/clients/{id}` | Update a client | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `DELETE` | `/api/clients/{id}` | Soft-delete a client | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `GET` | `/api/projects` | List projects within the caller's scope | `Authenticated (Scope-based)` |
| `POST` | `/api/projects` | Create a project | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `GET` | `/api/projects/{id}` | Read a project within the caller's scope | `Authenticated (Scope-based)` |
| `PUT` | `/api/projects/{id}` | Update a project | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |
| `DELETE` | `/api/projects/{id}` | Soft-delete a project | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER` |

### GET `/api/allocations`
**Purpose**: List allocations within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageAllocationResponse` |

---

### POST `/api/allocations`
**Purpose**: Create an allocation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Request Body (`application/json`)
**DTO Model**: `AllocationCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `projectId` | `string (uuid)` | **Yes** | - |
| `employeeId` | `string (uuid)` | **Yes** | - |
| `allocationPercentage` | `integer (int32)` | **Yes** | - |
| `roleTitle` | `string` | No | - |
| `startDate` | `string (date)` | **Yes** | - |
| `endDate` | `string (date)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AllocationResponse` |

---

### GET `/api/allocations/{id}`
**Purpose**: Read an allocation within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AllocationResponse` |

---

### PUT `/api/allocations/{id}`
**Purpose**: Update an allocation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `AllocationUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `projectId` | `string (uuid)` | **Yes** | - |
| `employeeId` | `string (uuid)` | **Yes** | - |
| `allocationPercentage` | `integer (int32)` | **Yes** | - |
| `roleTitle` | `string` | No | - |
| `startDate` | `string (date)` | **Yes** | - |
| `endDate` | `string (date)` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AllocationResponse` |

---

### DELETE `/api/allocations/{id}`
**Purpose**: Soft-delete an allocation  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/clients`
**Purpose**: List clients within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageClientResponse` |

---

### POST `/api/clients`
**Purpose**: Create a client  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Request Body (`application/json`)
**DTO Model**: `ClientCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | - |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ClientResponse` |

---

### GET `/api/clients/{id}`
**Purpose**: Read a client within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ClientResponse` |

---

### PUT `/api/clients/{id}`
**Purpose**: Update a client  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `ClientUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ClientResponse` |

---

### DELETE `/api/clients/{id}`
**Purpose**: Soft-delete a client  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/projects`
**Purpose**: List projects within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `q` | `query` | `string` | No | - |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageProjectResponse` |

---

### POST `/api/projects`
**Purpose**: Create a project  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Request Body (`application/json`)
**DTO Model**: `ProjectCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `clientId` | `string (uuid)` | **Yes** | - |
| `projectCode` | `string` | **Yes** | - |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |
| `projectManagerId` | `string (uuid)` | No | - |
| `status` | `string` | No | - |
| `startDate` | `string (date)` | **Yes** | - |
| `endDate` | `string (date)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProjectResponse` |

---

### GET `/api/projects/{id}`
**Purpose**: Read a project within the caller's scope  
**Access Control**: `Authenticated (Scope-based)`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProjectResponse` |

---

### PUT `/api/projects/{id}`
**Purpose**: Update a project  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `ProjectUpdateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `clientId` | `string (uuid)` | **Yes** | - |
| `name` | `string` | **Yes** | - |
| `description` | `string` | No | - |
| `projectManagerId` | `string (uuid)` | No | - |
| `status` | `string` | **Yes** | - |
| `startDate` | `string (date)` | **Yes** | - |
| `endDate` | `string (date)` | No | - |
| `version` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ProjectResponse` |

---

### DELETE `/api/projects/{id}`
**Purpose**: Soft-delete a project  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, PROJECT_MANAGER`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 05. Time, Attendance & Timesheets

Contains **18** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/attendance/employees/{employeeId}/clock-in` | Record clock-in time for an employee | `Authenticated` |
| `POST` | `/api/v1/attendance/employees/{employeeId}/clock-out` | Record clock-out time for an employee | `Authenticated` |
| `GET` | `/api/v1/attendance/employees/{employeeId}/latest` | Get the active clock-in session details for an employee | `Authenticated` |
| `GET` | `/api/v1/attendance/employees/{employeeId}/logs` | Get daily clock logs for an employee within a period | `Authenticated` |
| `GET` | `/api/v1/attendance/employees/{employeeId}/timesheets` | List timesheets for a specific employee | `Authenticated` |
| `POST` | `/api/v1/attendance/employees/{employeeId}/timesheets` | Create or update timesheet draft/lines | `Authenticated` |
| `GET` | `/api/v1/attendance/logs` | Get all tenant clock logs within a period | `Authenticated` |
| `POST` | `/api/v1/attendance/me/clock-in` | Record clock-in time for current logged-in employee | `Authenticated` |
| `POST` | `/api/v1/attendance/me/clock-out` | Record clock-out time for current logged-in employee | `Authenticated` |
| `GET` | `/api/v1/attendance/me/latest` | Get the active clock-in session details for current logged-in employee | `Authenticated` |
| `GET` | `/api/v1/attendance/me/logs` | Get daily clock logs for current logged-in employee within a period | `Authenticated` |
| `GET` | `/api/v1/attendance/me/timesheets` | List timesheets for current logged-in employee | `Authenticated` |
| `POST` | `/api/v1/attendance/me/timesheets` | Create or update timesheet draft/lines for current logged-in employee | `Authenticated` |
| `GET` | `/api/v1/attendance/timesheets/pending` | List timesheets pending approval | `Authenticated` |
| `GET` | `/api/v1/attendance/timesheets/{timesheetId}` | Get details of a specific timesheet | `Authenticated` |
| `POST` | `/api/v1/attendance/timesheets/{timesheetId}/approve` | Approve a timesheet | `Authenticated` |
| `POST` | `/api/v1/attendance/timesheets/{timesheetId}/reject` | Reject a timesheet | `Authenticated` |
| `POST` | `/api/v1/attendance/timesheets/{timesheetId}/submit` | Submit a timesheet for approval | `Authenticated` |

### POST `/api/v1/attendance/employees/{employeeId}/clock-in`
**Purpose**: Record clock-in time for an employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `ClockInRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `ipAddress` | `string` | No | - |
| `notes` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### POST `/api/v1/attendance/employees/{employeeId}/clock-out`
**Purpose**: Record clock-out time for an employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `ClockOutRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `notes` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### GET `/api/v1/attendance/employees/{employeeId}/latest`
**Purpose**: Get the active clock-in session details for an employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### GET `/api/v1/attendance/employees/{employeeId}/logs`
**Purpose**: Get daily clock logs for an employee within a period  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |
| `startIso` | `query` | `string` | **Yes** | - |
| `endIso` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/attendance/employees/{employeeId}/timesheets`
**Purpose**: List timesheets for a specific employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/attendance/employees/{employeeId}/timesheets`
**Purpose**: Create or update timesheet draft/lines  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `TimesheetCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `startDate` | `string (date)` | No | - |
| `endDate` | `string (date)` | No | - |
| `lines` | `Array<TimesheetLineCreateRequest>` | No | - |
| `submissionComments` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

### GET `/api/v1/attendance/logs`
**Purpose**: Get all tenant clock logs within a period  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `startIso` | `query` | `string` | **Yes** | - |
| `endIso` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/attendance/me/clock-in`
**Purpose**: Record clock-in time for current logged-in employee  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `ClockInRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `ipAddress` | `string` | No | - |
| `notes` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### POST `/api/v1/attendance/me/clock-out`
**Purpose**: Record clock-out time for current logged-in employee  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `ClockOutRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `notes` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### GET `/api/v1/attendance/me/latest`
**Purpose**: Get the active clock-in session details for current logged-in employee  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AttendanceResponse` |

---

### GET `/api/v1/attendance/me/logs`
**Purpose**: Get daily clock logs for current logged-in employee within a period  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `startIso` | `query` | `string` | **Yes** | - |
| `endIso` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/attendance/me/timesheets`
**Purpose**: List timesheets for current logged-in employee  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/attendance/me/timesheets`
**Purpose**: Create or update timesheet draft/lines for current logged-in employee  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `TimesheetCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `startDate` | `string (date)` | No | - |
| `endDate` | `string (date)` | No | - |
| `lines` | `Array<TimesheetLineCreateRequest>` | No | - |
| `submissionComments` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

### GET `/api/v1/attendance/timesheets/pending`
**Purpose**: List timesheets pending approval  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/attendance/timesheets/{timesheetId}`
**Purpose**: Get details of a specific timesheet  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `timesheetId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

### POST `/api/v1/attendance/timesheets/{timesheetId}/approve`
**Purpose**: Approve a timesheet  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `timesheetId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `TimesheetApproveRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `approvalComments` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

### POST `/api/v1/attendance/timesheets/{timesheetId}/reject`
**Purpose**: Reject a timesheet  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `timesheetId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `TimesheetApproveRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `approvalComments` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

### POST `/api/v1/attendance/timesheets/{timesheetId}/submit`
**Purpose**: Submit a timesheet for approval  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `timesheetId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `TimesheetResponse` |

---

## 06. Leave & Absence Management

Contains **8** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/admin/leave-accrual-runs` | Execute automated leave accrual calculation batch | `Authenticated` |
| `POST` | `/api/v1/admin/leave-adjustments` | Manually credit or debit employee leave balance | `Authenticated` |
| `GET` | `/api/v1/me/leave-balances` | Fetch current employee's remaining leave balances by type | `Authenticated` |
| `GET` | `/api/v1/me/leave-balances/{leaveTypeId}/ledger` | Fetch audit ledger of credits/debits for a leave type | `Authenticated` |
| `GET` | `/api/v1/me/leave-requests` | List leave absence requests filed by the current employee | `Authenticated` |
| `POST` | `/api/v1/me/leave-requests` | Draft a new leave absence request | `Authenticated` |
| `POST` | `/api/v1/me/leave-requests/{requestId}/cancel` | Cancel a draft or pending leave request | `Authenticated` |
| `POST` | `/api/v1/me/leave-requests/{requestId}/submit` | Submit draft leave request for manager approval | `Authenticated` |

### POST `/api/v1/admin/leave-accrual-runs`
**Purpose**: Execute automated leave accrual calculation batch  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `period` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/admin/leave-adjustments`
**Purpose**: Manually credit or debit employee leave balance  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `query` | `string (uuid)` | **Yes** | - |
| `leaveTypeId` | `query` | `string (uuid)` | **Yes** | - |
| `quantity` | `query` | `number` | **Yes** | - |
| `reason` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/me/leave-balances`
**Purpose**: Fetch current employee's remaining leave balances by type  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/me/leave-balances/{leaveTypeId}/ledger`
**Purpose**: Fetch audit ledger of credits/debits for a leave type  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `leaveTypeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/me/leave-requests`
**Purpose**: List leave absence requests filed by the current employee  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/me/leave-requests`
**Purpose**: Draft a new leave absence request  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `leaveTypeId` | `query` | `string (uuid)` | **Yes** | - |
| `startDate` | `query` | `string (date)` | **Yes** | - |
| `endDate` | `query` | `string (date)` | **Yes** | - |
| `reason` | `query` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `LeaveRequestResponseDto` |

---

### POST `/api/v1/me/leave-requests/{requestId}/cancel`
**Purpose**: Cancel a draft or pending leave request  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/me/leave-requests/{requestId}/submit`
**Purpose**: Submit draft leave request for manager approval  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requestId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 07. Compensation & Salary History

Contains **3** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/salaries` | Create a salary history record | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/salaries/employee/{employeeId}` | Read salary history for a specific employee | `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN / FINANCE_ADMIN` |
| `GET` | `/api/salaries/me` | Read own salary history | `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN / FINANCE_ADMIN` |

### POST `/api/salaries`
**Purpose**: Create a salary history record  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `SalaryCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - |
| `amount` | `number` | **Yes** | - |
| `currencyCode` | `string` | **Yes** | - |
| `effectiveFrom` | `string (date)` | **Yes** | - |
| `effectiveTo` | `string (date)` | No | - |
| `reason` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `SalaryResponse` |

---

### GET `/api/salaries/employee/{employeeId}`
**Purpose**: Read salary history for a specific employee  
**Access Control**: `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN / FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/salaries/me`
**Purpose**: Read own salary history  
**Access Control**: `EMPLOYEE (Self) or SUPER_ADMIN / HR_ADMIN / FINANCE_ADMIN`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

## 07. Payroll Engine & Gross-to-Net

Contains **12** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/payroll/employees/{employeeId}/payslips` | List historical payslips for an employee | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/me/payslips` | List historical payslips for the current logged-in employee | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs` | List all payroll periods for tenant | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `POST` | `/api/v1/payroll/runs` | Initialize a new draft payroll calculation period | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `POST` | `/api/v1/payroll/runs/{runId}/approve` | Approve computed payroll run (locks values) | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `POST` | `/api/v1/payroll/runs/{runId}/calculate` | Trigger gross-to-net computation for all active tenant employees | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs/{runId}/export/bank` | Export bank direct deposit disbursement CSV file for approved payroll run | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs/{runId}/export/ledger` | Export general ledger postings CSV file for approved payroll run | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `POST` | `/api/v1/payroll/runs/{runId}/pay` | Mark payroll run as PAID and finalize ledger records | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs/{runId}/payslips` | List all individual payslips generated inside a payroll run | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs/{runId}/payslips/employees/{employeeId}` | Get specific employee payslip details inside a payroll run | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |
| `GET` | `/api/v1/payroll/runs/{runId}/payslips/me` | Get current employee's payslip details inside a payroll run | `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN` |

### GET `/api/v1/payroll/employees/{employeeId}/payslips`
**Purpose**: List historical payslips for an employee  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/payroll/me/payslips`
**Purpose**: List historical payslips for the current logged-in employee  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/payroll/runs`
**Purpose**: List all payroll periods for tenant  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/payroll/runs`
**Purpose**: Initialize a new draft payroll calculation period  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Request Body (`application/json`)
**DTO Model**: `PayrollRunCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `periodStart` | `string (date)` | No | - |
| `periodEnd` | `string (date)` | No | - |
| `payoutDate` | `string (date)` | No | - |
| `runType` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayrollRunResponse` |

---

### POST `/api/v1/payroll/runs/{runId}/approve`
**Purpose**: Approve computed payroll run (locks values)  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayrollRunResponse` |

---

### POST `/api/v1/payroll/runs/{runId}/calculate`
**Purpose**: Trigger gross-to-net computation for all active tenant employees  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayrollRunResponse` |

---

### GET `/api/v1/payroll/runs/{runId}/export/bank`
**Purpose**: Export bank direct deposit disbursement CSV file for approved payroll run  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `string` |

---

### GET `/api/v1/payroll/runs/{runId}/export/ledger`
**Purpose**: Export general ledger postings CSV file for approved payroll run  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `string` |

---

### POST `/api/v1/payroll/runs/{runId}/pay`
**Purpose**: Mark payroll run as PAID and finalize ledger records  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayrollRunResponse` |

---

### GET `/api/v1/payroll/runs/{runId}/payslips`
**Purpose**: List all individual payslips generated inside a payroll run  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/payroll/runs/{runId}/payslips/employees/{employeeId}`
**Purpose**: Get specific employee payslip details inside a payroll run  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayslipResponse` |

---

### GET `/api/v1/payroll/runs/{runId}/payslips/me`
**Purpose**: Get current employee's payslip details inside a payroll run  
**Access Control**: `SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `runId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PayslipResponse` |

---

## 08. Recruitment - Candidate Applications

Contains **10** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/recruitment/applications/{applicationId}/feedbacks` | Get list of all feedback scorecards for an application | `Authenticated` |
| `GET` | `/api/v1/recruitment/applications/{applicationId}/interviews` | List all scheduled interviews for an application | `Authenticated` |
| `POST` | `/api/v1/recruitment/applications/{applicationId}/interviews` | Schedule an interview slot for a candidate application | `Authenticated` |
| `GET` | `/api/v1/recruitment/applications/{applicationId}/offers` | List all offers generated for a candidate application | `Authenticated` |
| `POST` | `/api/v1/recruitment/applications/{applicationId}/offers` | Create an offer letter draft for a candidate application | `Authenticated` |
| `GET` | `/api/v1/recruitment/applications/{id}` | Get application details by ID | `Authenticated` |
| `POST` | `/api/v1/recruitment/applications/{id}/move-stage` | Move candidate application to a different pipeline stage | `Authenticated` |
| `POST` | `/api/v1/recruitment/applications/{id}/reject` | Reject candidate application | `Authenticated` |
| `GET` | `/api/v1/recruitment/applications/{id}/resume-download` | Get presigned download URL for candidate resume | `Authenticated` |
| `POST` | `/api/v1/recruitment/applications/{id}/withdraw` | Withdraw candidate application | `Authenticated` |

### GET `/api/v1/recruitment/applications/{applicationId}/feedbacks`
**Purpose**: Get list of all feedback scorecards for an application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `applicationId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/recruitment/applications/{applicationId}/interviews`
**Purpose**: List all scheduled interviews for an application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `applicationId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/recruitment/applications/{applicationId}/interviews`
**Purpose**: Schedule an interview slot for a candidate application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `applicationId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `InterviewScheduleRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `interviewType` | `string` | No | - |
| `scheduledTime` | `string (date-time)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `InterviewResponse` |

---

### GET `/api/v1/recruitment/applications/{applicationId}/offers`
**Purpose**: List all offers generated for a candidate application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `applicationId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/recruitment/applications/{applicationId}/offers`
**Purpose**: Create an offer letter draft for a candidate application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `applicationId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `OfferCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `salaryAmount` | `number` | No | - |
| `currencyCode` | `string` | No | - |
| `startDate` | `string (date)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OfferResponse` |

---

### GET `/api/v1/recruitment/applications/{id}`
**Purpose**: Get application details by ID  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `CandidateApplicationResponse` |

---

### POST `/api/v1/recruitment/applications/{id}/move-stage`
**Purpose**: Move candidate application to a different pipeline stage  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |
| `stage` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/applications/{id}/reject`
**Purpose**: Reject candidate application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/recruitment/applications/{id}/resume-download`
**Purpose**: Get presigned download URL for candidate resume  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `object` |

---

### POST `/api/v1/recruitment/applications/{id}/withdraw`
**Purpose**: Withdraw candidate application  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 08. Recruitment - Internal Job Postings

Contains **4** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/recruitment/job-postings` | List all job postings | `Authenticated` |
| `GET` | `/api/v1/recruitment/job-postings/{id}` | Get internal job posting details by ID | `Authenticated` |
| `POST` | `/api/v1/recruitment/job-postings/{id}/publish` | Publish a job advertisement posting (Set to PUBLISHED) | `Authenticated` |
| `POST` | `/api/v1/recruitment/job-postings/{id}/unpublish` | Unpublish a job advertisement posting (Set to UNPUBLISHED) | `Authenticated` |

### GET `/api/v1/recruitment/job-postings`
**Purpose**: List all job postings  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/recruitment/job-postings/{id}`
**Purpose**: Get internal job posting details by ID  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobPostingResponse` |

---

### POST `/api/v1/recruitment/job-postings/{id}/publish`
**Purpose**: Publish a job advertisement posting (Set to PUBLISHED)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/job-postings/{id}/unpublish`
**Purpose**: Unpublish a job advertisement posting (Set to UNPUBLISHED)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 08. Recruitment - Interviews & Scorecards

Contains **4** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/recruitment/interviews/{id}/cancel` | Mark interview as CANCELLED | `Authenticated` |
| `POST` | `/api/v1/recruitment/interviews/{id}/complete` | Mark interview as COMPLETED | `Authenticated` |
| `GET` | `/api/v1/recruitment/interviews/{id}/feedback` | Get list of feedback scorecards for an interview slot | `Authenticated` |
| `POST` | `/api/v1/recruitment/interviews/{id}/feedback` | Submit score feedback scorecard for an interview | `Authenticated` |

### POST `/api/v1/recruitment/interviews/{id}/cancel`
**Purpose**: Mark interview as CANCELLED  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/interviews/{id}/complete`
**Purpose**: Mark interview as COMPLETED  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/recruitment/interviews/{id}/feedback`
**Purpose**: Get list of feedback scorecards for an interview slot  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/recruitment/interviews/{id}/feedback`
**Purpose**: Submit score feedback scorecard for an interview  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `InterviewFeedbackSubmitRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `interviewerId` | `string (uuid)` | No | - |
| `score` | `integer (int32)` | No | - |
| `recommendation` | `string` | No | - |
| `comments` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `InterviewFeedbackResponse` |

---

## 08. Recruitment - Job Offers

Contains **3** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/recruitment/offers/{id}/approve` | Approve offer letter | `Authenticated` |
| `POST` | `/api/v1/recruitment/offers/{id}/release` | Release/send offer letter to the candidate | `Authenticated` |
| `POST` | `/api/v1/recruitment/offers/{id}/submit` | Submit offer letter for HR approval | `Authenticated` |

### POST `/api/v1/recruitment/offers/{id}/approve`
**Purpose**: Approve offer letter  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/offers/{id}/release`
**Purpose**: Release/send offer letter to the candidate  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/offers/{id}/submit`
**Purpose**: Submit offer letter for HR approval  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 08. Recruitment - Job Requisitions

Contains **8** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/recruitment/requisitions` | List all job requisitions | `Authenticated` |
| `POST` | `/api/v1/recruitment/requisitions` | Create a new hiring requisition | `Authenticated` |
| `GET` | `/api/v1/recruitment/requisitions/{id}` | Get detailed requisition info by ID | `Authenticated` |
| `PATCH` | `/api/v1/recruitment/requisitions/{id}` | Update a requisition (Only permitted in DRAFT status) | `Authenticated` |
| `POST` | `/api/v1/recruitment/requisitions/{id}/approve` | Approve requisition (HR/Super Admin only) | `Authenticated` |
| `POST` | `/api/v1/recruitment/requisitions/{id}/close` | Close requisition | `Authenticated` |
| `POST` | `/api/v1/recruitment/requisitions/{id}/reject` | Reject requisition (HR/Super Admin only) | `Authenticated` |
| `POST` | `/api/v1/recruitment/requisitions/{id}/submit` | Submit requisition for approvals flow | `Authenticated` |

### GET `/api/v1/recruitment/requisitions`
**Purpose**: List all job requisitions  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/recruitment/requisitions`
**Purpose**: Create a new hiring requisition  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `JobRequisitionCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `jobTitle` | `string` | No | - |
| `departmentId` | `string (uuid)` | No | - |
| `designationId` | `string (uuid)` | No | - |
| `locationId` | `string (uuid)` | No | - |
| `legalEntityId` | `string (uuid)` | No | - |
| `employmentType` | `string` | No | - |
| `openingsCount` | `integer (int32)` | No | - |
| `hiringManagerId` | `string (uuid)` | No | - |
| `targetStartDate` | `string (date)` | No | - |
| `minSalary` | `number` | No | - |
| `maxSalary` | `number` | No | - |
| `currencyCode` | `string` | No | - |
| `requiredSkills` | `string` | No | - |
| `minExperienceYears` | `integer (int32)` | No | - |
| `description` | `string` | No | - |
| `justification` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobRequisitionResponse` |

---

### GET `/api/v1/recruitment/requisitions/{id}`
**Purpose**: Get detailed requisition info by ID  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobRequisitionResponse` |

---

### PATCH `/api/v1/recruitment/requisitions/{id}`
**Purpose**: Update a requisition (Only permitted in DRAFT status)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `JobRequisitionCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `jobTitle` | `string` | No | - |
| `departmentId` | `string (uuid)` | No | - |
| `designationId` | `string (uuid)` | No | - |
| `locationId` | `string (uuid)` | No | - |
| `legalEntityId` | `string (uuid)` | No | - |
| `employmentType` | `string` | No | - |
| `openingsCount` | `integer (int32)` | No | - |
| `hiringManagerId` | `string (uuid)` | No | - |
| `targetStartDate` | `string (date)` | No | - |
| `minSalary` | `number` | No | - |
| `maxSalary` | `number` | No | - |
| `currencyCode` | `string` | No | - |
| `requiredSkills` | `string` | No | - |
| `minExperienceYears` | `integer (int32)` | No | - |
| `description` | `string` | No | - |
| `justification` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobRequisitionResponse` |

---

### POST `/api/v1/recruitment/requisitions/{id}/approve`
**Purpose**: Approve requisition (HR/Super Admin only)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/requisitions/{id}/close`
**Purpose**: Close requisition  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/requisitions/{id}/reject`
**Purpose**: Reject requisition (HR/Super Admin only)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/recruitment/requisitions/{id}/submit`
**Purpose**: Submit requisition for approvals flow  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 08. Recruitment - Public Careers & Guest Portal

Contains **7** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/careers/jobs` | Get list of all active public postings | `Public / Anonymous` |
| `GET` | `/api/v1/careers/jobs/{publicId}` | Get public posting details by public UUID | `Public / Anonymous` |
| `POST` | `/api/v1/careers/jobs/{publicId}/applications` | Submit a public job application | `Public / Anonymous` |
| `GET` | `/api/v1/careers/offers/{secureToken}` | Get offer details by secure token | `Public / Anonymous` |
| `POST` | `/api/v1/careers/offers/{secureToken}/accept` | Accept the job offer, initiating pre-joining onboarding setup | `Public / Anonymous` |
| `POST` | `/api/v1/careers/offers/{secureToken}/reject` | Reject the job offer | `Public / Anonymous` |
| `POST` | `/api/v1/careers/resume/presign-upload` | Create resume presigned upload URL for guests | `Public / Anonymous` |

### GET `/api/v1/careers/jobs`
**Purpose**: Get list of all active public postings  
**Access Control**: `Public / Anonymous`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/careers/jobs/{publicId}`
**Purpose**: Get public posting details by public UUID  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `publicId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobPostingResponse` |

---

### POST `/api/v1/careers/jobs/{publicId}/applications`
**Purpose**: Submit a public job application  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `publicId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `CandidateApplicationCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `firstName` | `string` | No | - |
| `lastName` | `string` | No | - |
| `email` | `string` | No | - |
| `phone` | `string` | No | - |
| `resumeStorageKey` | `string` | No | - |
| `skills` | `string` | No | - |
| `profileSummary` | `string` | No | - |
| `coverLetter` | `string` | No | - |
| `source` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `CandidateApplicationResponse` |

---

### GET `/api/v1/careers/offers/{secureToken}`
**Purpose**: Get offer details by secure token  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `secureToken` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OfferResponse` |

---

### POST `/api/v1/careers/offers/{secureToken}/accept`
**Purpose**: Accept the job offer, initiating pre-joining onboarding setup  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `secureToken` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OfferResponse` |

---

### POST `/api/v1/careers/offers/{secureToken}/reject`
**Purpose**: Reject the job offer  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `secureToken` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OfferResponse` |

---

### POST `/api/v1/careers/resume/presign-upload`
**Purpose**: Create resume presigned upload URL for guests  
**Access Control**: `Public / Anonymous`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `filename` | `query` | `string` | **Yes** | - |
| `contentType` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PresignUploadResponse` |

---

## 08. Recruitment - Vacancies & Openings

Contains **3** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/recruitment/job-openings` | Manually open a job vacancy slot from an approved requisition | `Authenticated` |
| `POST` | `/api/v1/recruitment/job-openings/{id}/postings` | Create a job advertisement posting (DRAFT status) | `Authenticated` |
| `GET` | `/api/v1/recruitment/job-openings/{openingId}/applications` | List all applications submitted for a job opening | `Authenticated` |

### POST `/api/v1/recruitment/job-openings`
**Purpose**: Manually open a job vacancy slot from an approved requisition  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `requisitionId` | `query` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobOpeningResponse` |

---

### POST `/api/v1/recruitment/job-openings/{id}/postings`
**Purpose**: Create a job advertisement posting (DRAFT status)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `JobPostingCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `title` | `string` | No | - |
| `description` | `string` | No | - |
| `locationName` | `string` | No | - |
| `workArrangement` | `string` | No | - |
| `employmentType` | `string` | No | - |
| `applicationDeadline` | `string (date)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `JobPostingResponse` |

---

### GET `/api/v1/recruitment/job-openings/{openingId}/applications`
**Purpose**: List all applications submitted for a job opening  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `openingId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

## 09. Pre-Hire Onboarding & Candidate Activation

Contains **12** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/onboarding/assets/{assetId}/status` | Update status of asset allocation | `Authenticated` |
| `POST` | `/api/v1/onboarding/background-checks/{checkId}/status` | Update status of background verification | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires` | List all pre-hire candidate profiles | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires/{id}` | Get detailed pre-hire profile by ID | `Authenticated` |
| `POST` | `/api/v1/onboarding/pre-hires/{id}/activate` | Convert pre-hire candidate profile into active full Employee | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires/{id}/assets` | List asset provisioning allocations for pre-hire | `Authenticated` |
| `POST` | `/api/v1/onboarding/pre-hires/{id}/assets` | Request asset allocation for pre-hire | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires/{id}/background-checks` | List background check verifications for pre-hire | `Authenticated` |
| `POST` | `/api/v1/onboarding/pre-hires/{id}/background-checks` | Trigger background check verification for pre-hire | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires/{id}/plan` | Get onboarding checklist plan and tasks for a pre-hire | `Authenticated` |
| `POST` | `/api/v1/onboarding/pre-hires/{id}/plan` | Initialize onboarding tasks checklist template for pre-hire | `Authenticated` |
| `POST` | `/api/v1/onboarding/tasks/{taskId}/status` | Update progress status of an onboarding task | `Authenticated` |

### POST `/api/v1/onboarding/assets/{assetId}/status`
**Purpose**: Update status of asset allocation  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `assetId` | `path` | `string (uuid)` | **Yes** | - |
| `status` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AssetRequestResponse` |

---

### POST `/api/v1/onboarding/background-checks/{checkId}/status`
**Purpose**: Update status of background verification  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `checkId` | `path` | `string (uuid)` | **Yes** | - |
| `status` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `BackgroundCheckResponse` |

---

### GET `/api/v1/onboarding/pre-hires`
**Purpose**: List all pre-hire candidate profiles  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/onboarding/pre-hires/{id}`
**Purpose**: Get detailed pre-hire profile by ID  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PreHireResponse` |

---

### POST `/api/v1/onboarding/pre-hires/{id}/activate`
**Purpose**: Convert pre-hire candidate profile into active full Employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `EmployeeActivationRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `employeeCode` | `string` | **Yes** | - |
| `dateOfBirth` | `string (date)` | No | - |
| `phoneNumber` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeResponse` |

---

### GET `/api/v1/onboarding/pre-hires/{id}/assets`
**Purpose**: List asset provisioning allocations for pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/onboarding/pre-hires/{id}/assets`
**Purpose**: Request asset allocation for pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |
| `assetType` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `AssetRequestResponse` |

---

### GET `/api/v1/onboarding/pre-hires/{id}/background-checks`
**Purpose**: List background check verifications for pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/onboarding/pre-hires/{id}/background-checks`
**Purpose**: Trigger background check verification for pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `BackgroundCheckResponse` |

---

### GET `/api/v1/onboarding/pre-hires/{id}/plan`
**Purpose**: Get onboarding checklist plan and tasks for a pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingPlanResponse` |

---

### POST `/api/v1/onboarding/pre-hires/{id}/plan`
**Purpose**: Initialize onboarding tasks checklist template for pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |
| `templateName` | `query` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingPlanResponse` |

---

### POST `/api/v1/onboarding/tasks/{taskId}/status`
**Purpose**: Update progress status of an onboarding task  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `taskId` | `path` | `string (uuid)` | **Yes** | - |
| `status` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingTaskResponse` |

---

## 10. Document Management & MinIO S3

Contains **13** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/documents/employee/{employeeId}` | List documents for an employee | `Authenticated` |
| `POST` | `/api/documents/presign-upload` | Create document row and return presigned PUT URL | `Authenticated` |
| `DELETE` | `/api/documents/{documentId}` | Soft-delete a document (HR / super-admin) | `Authenticated` |
| `POST` | `/api/documents/{documentId}/complete` | Mark upload finished after client PUT to storage | `Authenticated` |
| `GET` | `/api/documents/{documentId}/presign-download` | Presigned GET URL for an uploaded document | `Authenticated` |
| `POST` | `/api/v1/documents/{documentId}/reject` | Reject candidate/employee submitted verification document | `Authenticated` |
| `POST` | `/api/v1/documents/{documentId}/verify` | Mark submitted document as verified and approved | `Authenticated` |
| `GET` | `/api/v1/me/documents` | List documents uploaded by the current employee | `Authenticated` |
| `POST` | `/api/v1/me/documents` | Initiate employee document upload with presigned S3 URL | `Authenticated` |
| `POST` | `/api/v1/onboarding/documents/{docId}/review` | Approve or reject verification document | `Authenticated` |
| `POST` | `/api/v1/onboarding/documents/{docId}/upload` | Upload verification document file | `Authenticated` |
| `GET` | `/api/v1/onboarding/pre-hires/{id}/documents` | List all required and uploaded onboarding verification documents | `Authenticated` |
| `POST` | `/api/v1/onboarding/pre-hires/{id}/documents` | Request a new verification document from pre-hire | `Authenticated` |

### GET `/api/documents/employee/{employeeId}`
**Purpose**: List documents for an employee  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/documents/presign-upload`
**Purpose**: Create document row and return presigned PUT URL  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `PresignUploadRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - |
| `documentType` | `string` | **Yes** | - |
| `contentType` | `string` | **Yes** | - |
| `originalFilename` | `string` | **Yes** | - |
| `sharable` | `boolean` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PresignUploadResponse` |

---

### DELETE `/api/documents/{documentId}`
**Purpose**: Soft-delete a document (HR / super-admin)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/documents/{documentId}/complete`
**Purpose**: Mark upload finished after client PUT to storage  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `path` | `string (uuid)` | **Yes** | - |

#### Request Body (`application/json`)
**DTO Model**: `CompleteUploadRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `sizeBytes` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `DocumentResponse` |

---

### GET `/api/documents/{documentId}/presign-download`
**Purpose**: Presigned GET URL for an uploaded document  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PresignDownloadResponse` |

---

### POST `/api/v1/documents/{documentId}/reject`
**Purpose**: Reject candidate/employee submitted verification document  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `path` | `string (uuid)` | **Yes** | - |
| `reason` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/documents/{documentId}/verify`
**Purpose**: Mark submitted document as verified and approved  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/me/documents`
**Purpose**: List documents uploaded by the current employee  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/me/documents`
**Purpose**: Initiate employee document upload with presigned S3 URL  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `classification` | `query` | `string` | **Yes** | - |
| `originalFilename` | `query` | `string` | **Yes** | - |
| `contentType` | `query` | `string` | **Yes** | - |
| `storageKey` | `query` | `string` | **Yes** | - |
| `sizeBytes` | `query` | `integer (int64)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `EmployeeDocument` |

---

### POST `/api/v1/onboarding/documents/{docId}/review`
**Purpose**: Approve or reject verification document  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `docId` | `path` | `string (uuid)` | **Yes** | - |
| `status` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingDocumentResponse` |

---

### POST `/api/v1/onboarding/documents/{docId}/upload`
**Purpose**: Upload verification document file  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `docId` | `path` | `string (uuid)` | **Yes** | - |
| `storageKey` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingDocumentResponse` |

---

### GET `/api/v1/onboarding/pre-hires/{id}/documents`
**Purpose**: List all required and uploaded onboarding verification documents  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/onboarding/pre-hires/{id}/documents`
**Purpose**: Request a new verification document from pre-hire  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |
| `documentType` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `OnboardingDocumentResponse` |

---

## 11. Company Announcements & Broadcasts

Contains **4** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/admin/announcements` | Publish a new company-wide announcement | `Authenticated` |
| `GET` | `/api/v1/me/announcements` | Fetch active announcements targeted to current user profile | `Authenticated` |
| `POST` | `/api/v1/me/announcements/{announcementId}/acknowledge` | acknowledge | `Authenticated` |
| `POST` | `/api/v1/me/announcements/{announcementId}/read` | read | `Authenticated` |

### POST `/api/v1/admin/announcements`
**Purpose**: Publish a new company-wide announcement  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `title` | `query` | `string` | **Yes** | - |
| `content` | `query` | `string` | **Yes** | - |
| `legalEntityId` | `query` | `string (uuid)` | No | - |
| `departmentId` | `query` | `string (uuid)` | No | - |
| `locationId` | `query` | `string (uuid)` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `Announcement` |

---

### GET `/api/v1/me/announcements`
**Purpose**: Fetch active announcements targeted to current user profile  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/me/announcements/{announcementId}/acknowledge`
**Purpose**: acknowledge  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `announcementId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### POST `/api/v1/me/announcements/{announcementId}/read`
**Purpose**: read  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `announcementId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

## 12. Workflows & Multi-Tier Approvals

Contains **7** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/approvals/requests` | List all pending approval requests | `Authenticated` |
| `POST` | `/api/approvals/requests` | Create an approval request (Manager initiates) | `Authenticated` |
| `GET` | `/api/approvals/requests/{id}` | Read an approval request | `Authenticated` |
| `POST` | `/api/approvals/requests/{id}/approve` | Approve an approval request (HR Admin action) | `Authenticated` |
| `POST` | `/api/approvals/requests/{id}/reject` | Reject an approval request (HR Admin action) | `Authenticated` |
| `GET` | `/api/v1/admin/workflows` | List workflow definitions and multi-tier approval stages | `Authenticated` |
| `POST` | `/api/v1/admin/workflows` | Create a new approval workflow policy | `Authenticated` |

### GET `/api/approvals/requests`
**Purpose**: List all pending approval requests  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/approvals/requests`
**Purpose**: Create an approval request (Manager initiates)  
**Access Control**: `Authenticated`  

#### Request Body (`application/json`)
**DTO Model**: `ApprovalRequestCreateRequest`

| Field | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - |
| `type` | `string` | **Yes** | - |
| `changeJson` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ApprovalRequestResponse` |

---

### GET `/api/approvals/requests/{id}`
**Purpose**: Read an approval request  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ApprovalRequestResponse` |

---

### POST `/api/approvals/requests/{id}/approve`
**Purpose**: Approve an approval request (HR Admin action)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ApprovalRequestResponse` |

---

### POST `/api/approvals/requests/{id}/reject`
**Purpose**: Reject an approval request (HR Admin action)  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ApprovalRequestResponse` |

---

### GET `/api/v1/admin/workflows`
**Purpose**: List workflow definitions and multi-tier approval stages  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `workflowType` | `query` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### POST `/api/v1/admin/workflows`
**Purpose**: Create a new approval workflow policy  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `workflowType` | `query` | `string` | **Yes** | - |
| `name` | `query` | `string` | **Yes** | - |

#### Request Body (`application/json`)
`string`

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `WorkflowDefinition` |

---

## 13. Manager Self-Service & Hierarchy

Contains **4** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/me/delegations` | Delegate manager approval authority to a designated peer | `Authenticated` |
| `DELETE` | `/api/v1/me/delegations/{delegationId}` | Revoke an active delegation of manager authority | `Authenticated` |
| `GET` | `/api/v1/me/team` | Fetch direct and indirect team reports hierarchy | `Authenticated` |
| `GET` | `/api/v1/me/team/leave-calendar` | Get team leave calendar and upcoming absences | `Authenticated` |

### POST `/api/v1/me/delegations`
**Purpose**: Delegate manager approval authority to a designated peer  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `delegateId` | `query` | `string (uuid)` | **Yes** | - |
| `startDate` | `query` | `string (date)` | **Yes** | - |
| `endDate` | `query` | `string (date)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `ManagerDelegation` |

---

### DELETE `/api/v1/me/delegations/{delegationId}`
**Purpose**: Revoke an active delegation of manager authority  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `delegationId` | `path` | `string (uuid)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | None / Void |

---

### GET `/api/v1/me/team`
**Purpose**: Fetch direct and indirect team reports hierarchy  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `scope` | `query` | `string` | No | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

### GET `/api/v1/me/team/leave-calendar`
**Purpose**: Get team leave calendar and upcoming absences  
**Access Control**: `Authenticated`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `startDate` | `query` | `string (date)` | **Yes** | - |
| `endDate` | `query` | `string (date)` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `array` |

---

## 14. System Audit Logging

Contains **1** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/audit-logs` | Paged audit trail (SUPER_ADMIN only; matrix §9) | `SUPER_ADMIN` |

### GET `/api/audit-logs`
**Purpose**: Paged audit trail (SUPER_ADMIN only; matrix §9)  
**Access Control**: `SUPER_ADMIN`  

#### Parameters
| Parameter | In | Type | Required | Description |
| :--- | :--- | :--- | :--- | :--- |
| `pageable` | `query` | `string` | **Yes** | - |

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `PageAuditLogEntryResponse` |

---

## 15. Executive Dashboard & Metrics

Contains **2** operational API endpoints.

| Method | Endpoint | Summary / Purpose | Access / Required Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard/bench` | Bench / utilization (org-wide or PM portfolio per matrix §10) | `Authenticated` |
| `GET` | `/api/dashboard/hr-overview` | Headcount, joiners, and leavers (scoped per RBAC matrix §10) | `Authenticated` |

### GET `/api/dashboard/bench`
**Purpose**: Bench / utilization (org-wide or PM portfolio per matrix §10)  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `BenchMetricsResponse` |

---

### GET `/api/dashboard/hr-overview`
**Purpose**: Headcount, joiners, and leavers (scoped per RBAC matrix §10)  
**Access Control**: `Authenticated`  

#### Responses
| Status Code | Description | Response Model |
| :--- | :--- | :--- |
| `HTTP 200` | OK | `HrOverviewResponse` |

---
