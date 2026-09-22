# ACME HRMS Complete Schema Reference Guide

> **Platform**: Acme Enterprise HRMS & Resource Management Platform  
> **Total Data Schemas**: 108 DTOs | **Standard**: OpenAPI 3.1.0 & Jackson JSON  
> **Purpose**: Authoritative reference of all request bodies and response payloads with realistic business data, field validation rules, and constraints.  

---

## 1. Schema Conventions & Type Rules

### 1.1 Why Generic Postman Placeholders Occur
When tools like Postman or Swagger UI import OpenAPI definitions, string fields default to generic `"string"` placeholders. In production, these fields must adhere to strict entity validation:
- **Codes** (`code`, `projectCode`, `employeeCode`): Uppercase alphanumeric strings, max 32 chars (e.g. `"APEX"`, `"PROJ-QUANTUM"`).
- **Names**: Formal corporate or individual names, max 128 chars (e.g. `"Apex Innovations LLC"`, `"Quantum AI Analytics Pipeline"`).
- **Descriptions**: Optional descriptive overviews, max 1024 chars.

### 1.2 Data Type Guidelines
| Type | Format | Example | Notes |
| :--- | :--- | :--- | :--- |
| **UUID** | `uuidv7` RFC 4122 | `"0190df0d-aaaa-7000-a000-000000000001"` | 36-char lowercase hexadecimal string |
| **Date** | `YYYY-MM-DD` | `"2026-02-01"` | ISO-8601 calendar date |
| **DateTime** | `ISO-8601 UTC` | `"2026-09-21T10:00:00Z"` | Instant in UTC timezone |
| **Money** | `Decimal` | `85000.00` | Stored as `numeric(18,4)`, paired with `currencyCode` (e.g. `USD`, `INR`) |
| **Enums** | Exact String | `"ACTIVE"` | Must match one of the predefined status values |

---

## 2. Table of Contents

- [**01. Authentication & Identity**](#01-authentication--identity) (19 schemas)
- [**02. Clients, Projects & Resource Allocations**](#02-clients-projects--resource-allocations) (12 schemas)
- [**03. Employee Master & Organizational Hierarchy**](#03-employee-master--organizational-hierarchy) (25 schemas)
- [**04. Employee Directory & Profiles**](#04-employee-directory--profiles) (2 schemas)
- [**05. Time, Attendance & Timesheets**](#05-time-attendance--timesheets) (3 schemas)
- [**06. Leaves & Absence Management**](#06-leaves--absence-management) (3 schemas)
- [**07. Payroll & Compensation**](#07-payroll--compensation) (6 schemas)
- [**08. Recruitment & Applicant Tracking System (ATS)**](#08-recruitment--applicant-tracking-system-(ats)) (13 schemas)
- [**09. Pre-Hire Onboarding & Candidate Activation**](#09-pre-hire-onboarding--candidate-activation) (5 schemas)
- [**11. Announcements & Workflow Approvals**](#11-announcements--workflow-approvals) (3 schemas)
- [**13. Common & Infrastructure DTOs**](#13-common--infrastructure-dtos) (17 schemas)

---

## 01. Authentication & Identity

Contains **19** data models.

### `Announcement`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `title` | `string` | No | - | - |
| `content` | `string` | No | - | - |
| `publishedAt` | `string (date-time)` | No | - | - |
| `expiresAt` | `string (date-time)` | No | - | - |
| `pinned` | `boolean` | No | - | - |
| `status` | `string` | No | - | - |
| `authorId` | `string (uuid)` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "title": "Senior Software Engineer",
  "content": "Example Value",
  "publishedAt": "2026-09-21T10:00:00Z",
  "expiresAt": "2026-09-21T10:00:00Z",
  "pinned": true,
  "status": "ACTIVE",
  "authorId": "0190df0d-aaaa-7000-a000-000000000001",
  "deleted": true
}
```

---

### `BenchMetricsResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `activeRosterSize` | `integer (int64)` | No | - | - |
| `fullyUtilizedCount` | `integer (int64)` | No | - | - |
| `underutilizedOrBenchedCount` | `integer (int64)` | No | - | - |
| `benchPercentage` | `number (double)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "activeRosterSize": 5000.0,
  "fullyUtilizedCount": 10,
  "underutilizedOrBenchedCount": 10,
  "benchPercentage": 85.5
}
```

---

### `Department`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "deleted": true
}
```

---

### `DepartmentCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | max 32 chars | - |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |

#### Realistic Business Example JSON
```json
{
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform."
}
```

---

### `DepartmentResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `DepartmentUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "version": 0
}
```

---

### `DocumentResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `documentType` | `string` | No | Values: `[PROFILE_PHOTO, OFFER_LETTER, ID_PROOF, OTHER]` | - |
| `restricted` | `boolean` | No | - | - |
| `sharable` | `boolean` | No | - | - |
| `originalFilename` | `string` | No | - | - |
| `contentType` | `string` | No | - | - |
| `sizeBytes` | `integer (int64)` | No | - | - |
| `uploadStatus` | `string` | No | Values: `[PENDING, UPLOADED]` | - |
| `createdAt` | `string (date-time)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "documentType": "PROFILE_PHOTO",
  "restricted": true,
  "sharable": true,
  "originalFilename": "Engineering & Technology",
  "contentType": "Example Value",
  "sizeBytes": 5000.0,
  "uploadStatus": "PENDING",
  "createdAt": "2026-09-21T10:00:00Z"
}
```

---

### `EmergencyContactDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `name` | `string` | No | - | - |
| `relationship` | `string` | No | - | - |
| `phone` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "name": "Apex Innovations LLC",
  "relationship": "Example Value",
  "phone": "Example Value"
}
```

---

### `EmployeeDocument`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `employee` | ``Employee`` | No | - | - |
| `documentType` | `string` | No | Values: `[PROFILE_PHOTO, OFFER_LETTER, ID_PROOF, OTHER]` | - |
| `restricted` | `boolean` | No | - | - |
| `sharable` | `boolean` | No | - | - |
| `storageKey` | `string` | No | - | - |
| `originalFilename` | `string` | No | - | - |
| `contentType` | `string` | No | - | - |
| `sizeBytes` | `integer (int64)` | No | - | - |
| `uploadStatus` | `string` | No | Values: `[PENDING, UPLOADED]` | - |
| `classification` | `string` | No | - | - |
| `verificationStatus` | `string` | No | - | - |
| `expiryDate` | `string (date)` | No | - | - |
| `issuedDate` | `string (date)` | No | - | - |
| `verifiedBy` | `string (uuid)` | No | - | - |
| `verifiedAt` | `string (date-time)` | No | - | - |
| `rejectionReason` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "employee": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "employeeCode": "EMP-001",
    "firstName": "Emp",
    "lastName": "Loyee",
    "email": "employee@acme.local",
    "phoneNumber": "+1 (415) 555-0199",
    "dateOfBirth": "1992-06-15",
    "dateOfJoining": "2026-01-01",
    "employmentStatus": "ACTIVE",
    "passwordHash": "Example Value",
    "roles": [
      "SUPER_ADMIN",
      "HR_ADMIN"
    ],
    "department": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "deleted": true
    },
    "designation": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "title": "Senior Software Engineer",
      "level": "Example Value",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "deleted": true
    },
    "location": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "city": "Example Value",
      "country": "Example Value",
      "deleted": true
    },
    "legalEntity": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "deleted": true
    },
    "manager": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "employeeCode": "EMP-001",
      "firstName": "Emp",
      "lastName": "Loyee",
      "email": "employee@acme.local",
      "phoneNumber": "+1 (415) 555-0199",
      "dateOfBirth": "1992-06-15",
      "dateOfJoining": "2026-01-01",
      "employmentStatus": "ACTIVE",
      "passwordHash": "Example Value",
      "roles": [
        "SUPER_ADMIN",
        "HR_ADMIN"
      ],
      "department": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
        "deleted": true
      },
      "designation": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "title": "Senior Software Engineer",
        "level": "Example Value",
        "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
        "deleted": true
      },
      "location": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "city": "Example Value",
        "country": "Example Value",
        "deleted": true
      },
      "legalEntity": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "deleted": true
      },
      "manager": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "employeeCode": "EMP-001",
        "firstName": "Emp",
        "lastName": "Loyee",
        "email": "employee@acme.local",
        "phoneNumber": "+1 (415) 555-0199",
        "dateOfBirth": "1992-06-15",
        "dateOfJoining": "2026-01-01",
        "employmentStatus": "ACTIVE",
        "passwordHash": "Example Value",
        "roles": [
          "SUPER_ADMIN",
          "HR_ADMIN"
        ],
        "department": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "description": {},
          "deleted": {}
        },
        "designation": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "title": {},
          "level": {},
          "description": {},
          "deleted": {}
        },
        "location": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "city": {},
          "country": {},
          "deleted": {}
        },
        "legalEntity": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "deleted": {}
        },
        "manager": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "employeeCode": {},
          "firstName": {},
          "lastName": {},
          "email": {},
          "phoneNumber": {},
          "dateOfBirth": {},
          "dateOfJoining": {},
          "employmentStatus": {},
          "passwordHash": {},
          "roles": {},
          "department": {},
          "designation": {},
          "location": {},
          "legalEntity": {},
          "manager": {},
          "preferredName": {},
          "bankAccountNumber": {},
          "taxId": {},
          "deleted": {}
        },
        "preferredName": "Engineering & Technology",
        "bankAccountNumber": "Example Value",
        "taxId": "Example Value",
        "deleted": true
      },
      "preferredName": "Engineering & Technology",
      "bankAccountNumber": "Example Value",
      "taxId": "Example Value",
      "deleted": true
    },
    "preferredName": "Engineering & Technology",
    "bankAccountNumber": "Example Value",
    "taxId": "Example Value",
    "deleted": true
  },
  "documentType": "PROFILE_PHOTO",
  "restricted": true,
  "sharable": true,
  "storageKey": "Example Value",
  "originalFilename": "Engineering & Technology",
  "contentType": "Example Value",
  "sizeBytes": 5000.0,
  "uploadStatus": "PENDING",
  "classification": "Example Value",
  "verificationStatus": "Example Value",
  "expiryDate": "2026-02-01",
  "issuedDate": "2026-02-01",
  "verifiedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "verifiedAt": "2026-09-21T10:00:00Z",
  "rejectionReason": "Completed sprint deliverables on schedule.",
  "deleted": true
}
```

---

### `LoginRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `email` | `string (email)` | **Yes** | min 1 chars | - |
| `password` | `string` | **Yes** | min 1 chars | - |

#### Realistic Business Example JSON
```json
{
  "email": "employee@acme.local",
  "password": "Admin#12345"
}
```

---

### `LoginResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `token` | `string` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `email` | `string` | No | - | - |
| `roles` | `Array<string>` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "token": "eyJraWQiOiJocm1zLWtleSIsImFsZyI6IkhTMjU2In0.eyJzdWIiOiIwMTkwZGYwZC1mZmZmLTdjMDAtYTAwMC0wMDAwMDAwMDAwMDEiLCJlbWFpbCI6ImFkbWluQGFjbWUubG9jYWwiLCJyb2xlcyI6WyJTVVBFUl9BRE1JTiJdfQ.sampleTokenSignature",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "email": "employee@acme.local",
  "roles": [
    "SUPER_ADMIN",
    "HR_ADMIN"
  ]
}
```

---

### `MeResponse`
**Classification**: `HTTP Response Payload`  
**Overview**: Identity and roles of the currently authenticated caller, as parsed from the bearer JWT.  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `subjectUuid` | `string (uuid)` | No | - | Keycloak subject UUID (sub claim). |
| `username` | `string` | No | - | preferred_username claim, if present. |
| `email` | `string` | No | - | email claim, if present. |
| `roles` | `Array<string>` | No | - | Realm roles from realm_access.roles, in alphabetical order. |
| `requestId` | `string` | No | - | X-Request-Id of this request, for log correlation. |

#### Realistic Business Example JSON
```json
{
  "subjectUuid": "0190df0d-aaaa-7000-a000-000000000001",
  "username": "Engineering & Technology",
  "email": "employee@acme.local",
  "roles": [
    "SUPER_ADMIN",
    "HR_ADMIN"
  ],
  "requestId": "Example Value"
}
```

---

### `OnboardingDocumentResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `preHireId` | `string (uuid)` | No | - | - |
| `documentType` | `string` | No | - | - |
| `storageKey` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "preHireId": "0190df0d-aaaa-7000-a000-000000000001",
  "documentType": "Example Value",
  "storageKey": "Example Value",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `PageDepartmentResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<DepartmentResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `TimesheetApproveRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `approvalComments` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "approvalComments": "Completed sprint deliverables on schedule."
}
```

---

### `TimesheetCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `lines` | `Array<TimesheetLineCreateRequest>` | No | - | - |
| `submissionComments` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "lines": [
    {
      "dayDate": "2026-02-01",
      "hoursWorked": 5000.0,
      "notes": "Completed sprint deliverables on schedule."
    }
  ],
  "submissionComments": "Completed sprint deliverables on schedule."
}
```

---

### `TimesheetLineCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `dayDate` | `string (date)` | No | - | - |
| `hoursWorked` | `number` | No | - | - |
| `notes` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "dayDate": "2026-02-01",
  "hoursWorked": 5000.0,
  "notes": "Completed sprint deliverables on schedule."
}
```

---

### `TimesheetLineResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `dayDate` | `string (date)` | No | - | - |
| `hoursWorked` | `number` | No | - | - |
| `notes` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "dayDate": "2026-02-01",
  "hoursWorked": 5000.0,
  "notes": "Completed sprint deliverables on schedule."
}
```

---

### `TimesheetResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `employeeName` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `totalHours` | `number` | No | - | - |
| `status` | `string` | No | - | - |
| `approvedById` | `string (uuid)` | No | - | - |
| `approvedByName` | `string` | No | - | - |
| `approvedAt` | `string (date-time)` | No | - | - |
| `submissionComments` | `string` | No | - | - |
| `approvalComments` | `string` | No | - | - |
| `lines` | `Array<TimesheetLineResponse>` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "employeeName": "Emp Loyee",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "totalHours": 40.0,
  "status": "ACTIVE",
  "approvedById": "0190df0d-aaaa-7000-a000-000000000001",
  "approvedByName": "Engineering & Technology",
  "approvedAt": "2026-09-21T10:00:00Z",
  "submissionComments": "Completed sprint deliverables on schedule.",
  "approvalComments": "Completed sprint deliverables on schedule.",
  "lines": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "dayDate": "2026-02-01",
      "hoursWorked": 5000.0,
      "notes": "Completed sprint deliverables on schedule."
    }
  ]
}
```

---

## 02. Clients, Projects & Resource Allocations

Contains **12** data models.

### `AllocationCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `projectId` | `string (uuid)` | **Yes** | - | - |
| `employeeId` | `string (uuid)` | **Yes** | - | - |
| `allocationPercentage` | `integer (int32)` | **Yes** | min: 1, max: 100 | - |
| `roleTitle` | `string` | No | max 128 chars | - |
| `startDate` | `string (date)` | **Yes** | - | - |
| `endDate` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "projectId": "0190df0d-9999-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "allocationPercentage": 100,
  "roleTitle": "Lead Full-Stack Engineer",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30"
}
```

---

### `AllocationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `projectId` | `string (uuid)` | No | - | - |
| `projectCode` | `string` | No | - | - |
| `projectName` | `string` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `employeeName` | `string` | No | - | - |
| `allocationPercentage` | `integer (int32)` | No | - | - |
| `roleTitle` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "projectId": "0190df0d-9999-7000-a000-000000000001",
  "projectCode": "PROJ-QUANTUM",
  "projectName": "Quantum AI Analytics Pipeline",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "employeeName": "Emp Loyee",
  "allocationPercentage": 100,
  "roleTitle": "Lead Full-Stack Engineer",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `AllocationUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `projectId` | `string (uuid)` | **Yes** | - | - |
| `employeeId` | `string (uuid)` | **Yes** | - | - |
| `allocationPercentage` | `integer (int32)` | **Yes** | min: 1, max: 100 | - |
| `roleTitle` | `string` | No | max 128 chars | - |
| `startDate` | `string (date)` | **Yes** | - | - |
| `endDate` | `string (date)` | No | - | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "projectId": "0190df0d-9999-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "allocationPercentage": 100,
  "roleTitle": "Lead Full-Stack Engineer",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "version": 0
}
```

---

### `ClientCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | max 32 chars | - |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |

#### Realistic Business Example JSON
```json
{
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform."
}
```

---

### `ClientResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `ClientUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "version": 0
}
```

---

### `PageAllocationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<AllocationResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "projectId": "0190df0d-9999-7000-a000-000000000001",
      "projectCode": "PROJ-QUANTUM",
      "projectName": "Quantum AI Analytics Pipeline",
      "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
      "employeeName": "Emp Loyee",
      "allocationPercentage": 100,
      "roleTitle": "Lead Full-Stack Engineer",
      "startDate": "2026-02-01",
      "endDate": "2026-11-30",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageClientResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<ClientResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageProjectResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<ProjectResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "clientId": "0190df0d-aaaa-7000-a000-000000000001",
      "clientName": "Apex Innovations LLC",
      "projectCode": "PROJ-QUANTUM",
      "name": "Apex Innovations LLC",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "projectManagerId": "0190df0d-ffff-7c00-a000-000000000001",
      "projectManagerName": "Engineering & Technology",
      "status": "ACTIVE",
      "startDate": "2026-02-01",
      "endDate": "2026-11-30",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `ProjectCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `clientId` | `string (uuid)` | **Yes** | - | - |
| `projectCode` | `string` | **Yes** | max 32 chars | - |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |
| `projectManagerId` | `string (uuid)` | No | - | - |
| `status` | `string` | No | Values: `[PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED]` | - |
| `startDate` | `string (date)` | **Yes** | - | - |
| `endDate` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "clientId": "0190df0d-aaaa-7000-a000-000000000001",
  "projectCode": "PROJ-QUANTUM",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "projectManagerId": "0190df0d-ffff-7c00-a000-000000000001",
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30"
}
```

---

### `ProjectResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `clientId` | `string (uuid)` | No | - | - |
| `clientName` | `string` | No | - | - |
| `projectCode` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `projectManagerId` | `string (uuid)` | No | - | - |
| `projectManagerName` | `string` | No | - | - |
| `status` | `string` | No | Values: `[PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED]` | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "clientId": "0190df0d-aaaa-7000-a000-000000000001",
  "clientName": "Apex Innovations LLC",
  "projectCode": "PROJ-QUANTUM",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "projectManagerId": "0190df0d-ffff-7c00-a000-000000000001",
  "projectManagerName": "Engineering & Technology",
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `ProjectUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `clientId` | `string (uuid)` | **Yes** | - | - |
| `name` | `string` | **Yes** | max 128 chars | - |
| `description` | `string` | No | max 1024 chars | - |
| `projectManagerId` | `string (uuid)` | No | - | - |
| `status` | `string` | **Yes** | Values: `[PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED]` | - |
| `startDate` | `string (date)` | **Yes** | - | - |
| `endDate` | `string (date)` | No | - | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "clientId": "0190df0d-aaaa-7000-a000-000000000001",
  "name": "Apex Innovations LLC",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "projectManagerId": "0190df0d-ffff-7c00-a000-000000000001",
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "version": 0
}
```

---

## 03. Employee Master & Organizational Hierarchy

Contains **25** data models.

### `Designation`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `title` | `string` | No | - | - |
| `level` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "title": "Senior Software Engineer",
  "level": "Example Value",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "deleted": true
}
```

---

### `DesignationCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `title` | `string` | **Yes** | max 128 chars | - |
| `level` | `string` | No | max 16 chars | - |
| `description` | `string` | No | max 1024 chars | - |

#### Realistic Business Example JSON
```json
{
  "title": "Senior Software Engineer",
  "level": "Example Value",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform."
}
```

---

### `DesignationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `title` | `string` | No | - | - |
| `level` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "title": "Senior Software Engineer",
  "level": "Example Value",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `DesignationUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `title` | `string` | **Yes** | max 128 chars | - |
| `level` | `string` | No | max 16 chars | - |
| `description` | `string` | No | max 1024 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "title": "Senior Software Engineer",
  "level": "Example Value",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "version": 0
}
```

---

### `DirectoryEmployeeResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `fullName` | `string` | No | - | - |
| `designationTitle` | `string` | No | - | - |
| `departmentName` | `string` | No | - | - |
| `locationName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `phoneNumber` | `string` | No | - | - |
| `managerName` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "fullName": "Emp Loyee",
  "designationTitle": "Senior Software Engineer",
  "departmentName": "Engineering & Technology",
  "locationName": "Bengaluru Headquarters",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "managerName": "Engineering & Technology"
}
```

---

### `Employee`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `employeeCode` | `string` | No | - | - |
| `firstName` | `string` | No | - | - |
| `lastName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `phoneNumber` | `string` | No | - | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `dateOfJoining` | `string (date)` | No | - | - |
| `employmentStatus` | `string` | No | Values: `[ACTIVE, ON_LEAVE, TERMINATED, RESIGNED, ABSCONDED]` | - |
| `passwordHash` | `string` | No | - | - |
| `roles` | `Array<string>` | No | - | - |
| `department` | ``Department`` | No | - | - |
| `designation` | ``Designation`` | No | - | - |
| `location` | ``Location`` | No | - | - |
| `legalEntity` | ``LegalEntity`` | No | - | - |
| `manager` | ``Employee`` | No | - | - |
| `preferredName` | `string` | No | - | - |
| `bankAccountNumber` | `string` | No | - | - |
| `taxId` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "employeeCode": "EMP-001",
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "dateOfBirth": "1992-06-15",
  "dateOfJoining": "2026-01-01",
  "employmentStatus": "ACTIVE",
  "passwordHash": "Example Value",
  "roles": [
    "SUPER_ADMIN",
    "HR_ADMIN"
  ],
  "department": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "code": "APEX",
    "name": "Apex Innovations LLC",
    "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
    "deleted": true
  },
  "designation": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "title": "Senior Software Engineer",
    "level": "Example Value",
    "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
    "deleted": true
  },
  "location": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "code": "APEX",
    "name": "Apex Innovations LLC",
    "city": "Example Value",
    "country": "Example Value",
    "deleted": true
  },
  "legalEntity": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "code": "APEX",
    "name": "Apex Innovations LLC",
    "deleted": true
  },
  "manager": {
    "id": "0190df0d-aaaa-7000-a000-000000000001",
    "createdAt": "2026-09-21T10:00:00Z",
    "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
    "updatedAt": "2026-09-21T10:00:00Z",
    "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
    "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
    "deletedAt": "2026-09-21T10:00:00Z",
    "version": 0,
    "employeeCode": "EMP-001",
    "firstName": "Emp",
    "lastName": "Loyee",
    "email": "employee@acme.local",
    "phoneNumber": "+1 (415) 555-0199",
    "dateOfBirth": "1992-06-15",
    "dateOfJoining": "2026-01-01",
    "employmentStatus": "ACTIVE",
    "passwordHash": "Example Value",
    "roles": [
      "SUPER_ADMIN",
      "HR_ADMIN"
    ],
    "department": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "deleted": true
    },
    "designation": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "title": "Senior Software Engineer",
      "level": "Example Value",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "deleted": true
    },
    "location": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "city": "Example Value",
      "country": "Example Value",
      "deleted": true
    },
    "legalEntity": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "deleted": true
    },
    "manager": {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "createdAt": "2026-09-21T10:00:00Z",
      "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
      "updatedAt": "2026-09-21T10:00:00Z",
      "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
      "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
      "deletedAt": "2026-09-21T10:00:00Z",
      "version": 0,
      "employeeCode": "EMP-001",
      "firstName": "Emp",
      "lastName": "Loyee",
      "email": "employee@acme.local",
      "phoneNumber": "+1 (415) 555-0199",
      "dateOfBirth": "1992-06-15",
      "dateOfJoining": "2026-01-01",
      "employmentStatus": "ACTIVE",
      "passwordHash": "Example Value",
      "roles": [
        "SUPER_ADMIN",
        "HR_ADMIN"
      ],
      "department": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
        "deleted": true
      },
      "designation": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "title": "Senior Software Engineer",
        "level": "Example Value",
        "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
        "deleted": true
      },
      "location": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "city": "Example Value",
        "country": "Example Value",
        "deleted": true
      },
      "legalEntity": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "code": "APEX",
        "name": "Apex Innovations LLC",
        "deleted": true
      },
      "manager": {
        "id": "0190df0d-aaaa-7000-a000-000000000001",
        "createdAt": "2026-09-21T10:00:00Z",
        "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
        "updatedAt": "2026-09-21T10:00:00Z",
        "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
        "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
        "deletedAt": "2026-09-21T10:00:00Z",
        "version": 0,
        "employeeCode": "EMP-001",
        "firstName": "Emp",
        "lastName": "Loyee",
        "email": "employee@acme.local",
        "phoneNumber": "+1 (415) 555-0199",
        "dateOfBirth": "1992-06-15",
        "dateOfJoining": "2026-01-01",
        "employmentStatus": "ACTIVE",
        "passwordHash": "Example Value",
        "roles": [
          "SUPER_ADMIN",
          "HR_ADMIN"
        ],
        "department": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "description": {},
          "deleted": {}
        },
        "designation": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "title": {},
          "level": {},
          "description": {},
          "deleted": {}
        },
        "location": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "city": {},
          "country": {},
          "deleted": {}
        },
        "legalEntity": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "code": {},
          "name": {},
          "deleted": {}
        },
        "manager": {
          "id": {},
          "createdAt": {},
          "createdBy": {},
          "updatedAt": {},
          "updatedBy": {},
          "tenantId": {},
          "deletedAt": {},
          "version": {},
          "employeeCode": {},
          "firstName": {},
          "lastName": {},
          "email": {},
          "phoneNumber": {},
          "dateOfBirth": {},
          "dateOfJoining": {},
          "employmentStatus": {},
          "passwordHash": {},
          "roles": {},
          "department": {},
          "designation": {},
          "location": {},
          "legalEntity": {},
          "manager": {},
          "preferredName": {},
          "bankAccountNumber": {},
          "taxId": {},
          "deleted": {}
        },
        "preferredName": "Engineering & Technology",
        "bankAccountNumber": "Example Value",
        "taxId": "Example Value",
        "deleted": true
      },
      "preferredName": "Engineering & Technology",
      "bankAccountNumber": "Example Value",
      "taxId": "Example Value",
      "deleted": true
    },
    "preferredName": "Engineering & Technology",
    "bankAccountNumber": "Example Value",
    "taxId": "Example Value",
    "deleted": true
  },
  "preferredName": "Engineering & Technology",
  "bankAccountNumber": "Example Value",
  "taxId": "Example Value",
  "deleted": true
}
```

---

### `EmployeeActivationRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeCode` | `string` | **Yes** | min 1 chars | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `phoneNumber` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "employeeCode": "EMP-001",
  "dateOfBirth": "1992-06-15",
  "phoneNumber": "+1 (415) 555-0199"
}
```

---

### `EmployeeContactUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `phoneNumber` | `string` | No | max 32 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "phoneNumber": "+1 (415) 555-0199",
  "version": 0
}
```

---

### `EmployeeCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeCode` | `string` | **Yes** | max 32 chars | - |
| `firstName` | `string` | **Yes** | max 64 chars | - |
| `lastName` | `string` | **Yes** | max 64 chars | - |
| `email` | `string (email)` | **Yes** | max 254 chars | - |
| `phoneNumber` | `string` | No | max 32 chars | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `dateOfJoining` | `string (date)` | **Yes** | - | - |
| `employmentStatus` | `string` | No | Values: `[ACTIVE, ON_LEAVE, TERMINATED, RESIGNED, ABSCONDED]` | - |
| `keycloakUserId` | `string (uuid)` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `legalEntityId` | `string (uuid)` | No | - | - |
| `managerId` | `string (uuid)` | No | - | - |
| `roles` | `Array<string>` | No | - | - |
| `keycloakPassword` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "employeeCode": "EMP-001",
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "dateOfBirth": "1992-06-15",
  "dateOfJoining": "2026-01-01",
  "employmentStatus": "ACTIVE",
  "keycloakUserId": "0190df0d-aaaa-7000-a000-000000000001",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "legalEntityId": "0190df0d-bbbb-7000-a000-000000000001",
  "managerId": "0190df0d-ffff-7c00-a000-000000000001",
  "roles": [
    "SUPER_ADMIN",
    "HR_ADMIN"
  ],
  "keycloakPassword": "Temp#Password123"
}
```

---

### `EmployeeResponse`
**Classification**: `HTTP Response Payload`  
**Overview**: Full employee read. Some fields are role-conditional.  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeCode` | `string` | No | - | - |
| `firstName` | `string` | No | - | - |
| `lastName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `phoneNumber` | `string` | No | - | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `dateOfJoining` | `string (date)` | No | - | - |
| `employmentStatus` | `string` | No | Values: `[ACTIVE, ON_LEAVE, TERMINATED, RESIGNED, ABSCONDED]` | - |
| `keycloakUserId` | `string (uuid)` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `departmentName` | `string` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `designationTitle` | `string` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `locationName` | `string` | No | - | - |
| `legalEntityId` | `string (uuid)` | No | - | - |
| `legalEntityName` | `string` | No | - | - |
| `managerId` | `string (uuid)` | No | - | - |
| `managerName` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `roles` | `Array<string>` | No | - | - |
| `tempPassword` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeCode": "EMP-001",
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "dateOfBirth": "1992-06-15",
  "dateOfJoining": "2026-01-01",
  "employmentStatus": "ACTIVE",
  "keycloakUserId": "0190df0d-aaaa-7000-a000-000000000001",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "departmentName": "Engineering & Technology",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "designationTitle": "Senior Software Engineer",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "locationName": "Bengaluru Headquarters",
  "legalEntityId": "0190df0d-bbbb-7000-a000-000000000001",
  "legalEntityName": "Acme Corporation Global Inc.",
  "managerId": "0190df0d-ffff-7c00-a000-000000000001",
  "managerName": "Engineering & Technology",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "roles": [
    "SUPER_ADMIN",
    "HR_ADMIN"
  ],
  "tempPassword": "Example Value"
}
```

---

### `EmployeeSummary`
**Classification**: `HTTP Response Payload`  
**Overview**: List-view employee record. Omits sensitive personal details — see EmployeeResponse for the full read.  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeCode` | `string` | No | - | - |
| `firstName` | `string` | No | - | - |
| `lastName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `departmentName` | `string` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `designationTitle` | `string` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `locationName` | `string` | No | - | - |
| `dateOfJoining` | `string (date)` | No | - | - |
| `employmentStatus` | `string` | No | Values: `[ACTIVE, ON_LEAVE, TERMINATED, RESIGNED, ABSCONDED]` | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeCode": "EMP-001",
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "departmentName": "Engineering & Technology",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "designationTitle": "Senior Software Engineer",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "locationName": "Bengaluru Headquarters",
  "dateOfJoining": "2026-01-01",
  "employmentStatus": "ACTIVE"
}
```

---

### `EmployeeUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `firstName` | `string` | **Yes** | max 64 chars | - |
| `lastName` | `string` | **Yes** | max 64 chars | - |
| `email` | `string (email)` | **Yes** | max 254 chars | - |
| `phoneNumber` | `string` | No | max 32 chars | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `dateOfJoining` | `string (date)` | **Yes** | - | - |
| `employmentStatus` | `string` | **Yes** | Values: `[ACTIVE, ON_LEAVE, TERMINATED, RESIGNED, ABSCONDED]` | - |
| `keycloakUserId` | `string (uuid)` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `legalEntityId` | `string (uuid)` | No | - | - |
| `managerId` | `string (uuid)` | No | - | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "dateOfBirth": "1992-06-15",
  "dateOfJoining": "2026-01-01",
  "employmentStatus": "ACTIVE",
  "keycloakUserId": "0190df0d-aaaa-7000-a000-000000000001",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "legalEntityId": "0190df0d-bbbb-7000-a000-000000000001",
  "managerId": "0190df0d-ffff-7c00-a000-000000000001",
  "version": 0
}
```

---

### `LegalEntity`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "deleted": true
}
```

---

### `LegalEntityCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | max 16 chars, min 2 chars | - |
| `name` | `string` | **Yes** | max 128 chars, min 2 chars | - |

#### Realistic Business Example JSON
```json
{
  "code": "APEX",
  "name": "Apex Innovations LLC"
}
```

---

### `LegalEntityResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "version": 0
}
```

---

### `LegalEntityUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | max 128 chars, min 2 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "name": "Apex Innovations LLC",
  "version": 0
}
```

---

### `Location`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `city` | `string` | No | - | - |
| `country` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "city": "Example Value",
  "country": "Example Value",
  "deleted": true
}
```

---

### `LocationCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `code` | `string` | **Yes** | max 32 chars | - |
| `name` | `string` | **Yes** | max 128 chars | - |
| `city` | `string` | No | max 64 chars | - |
| `country` | `string` | No | max 64 chars | - |

#### Realistic Business Example JSON
```json
{
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "city": "Example Value",
  "country": "Example Value"
}
```

---

### `LocationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `code` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `city` | `string` | No | - | - |
| `country` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "code": "APEX",
  "name": "Apex Innovations LLC",
  "city": "Example Value",
  "country": "Example Value",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `LocationUpdateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `name` | `string` | **Yes** | max 128 chars | - |
| `city` | `string` | No | max 64 chars | - |
| `country` | `string` | No | max 64 chars | - |
| `version` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "name": "Apex Innovations LLC",
  "city": "Example Value",
  "country": "Example Value",
  "version": 0
}
```

---

### `PageDesignationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<DesignationResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "title": "Senior Software Engineer",
      "level": "Example Value",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageDirectoryEmployeeResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<DirectoryEmployeeResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "fullName": "Emp Loyee",
      "designationTitle": "Senior Software Engineer",
      "departmentName": "Engineering & Technology",
      "locationName": "Bengaluru Headquarters",
      "email": "employee@acme.local",
      "phoneNumber": "+1 (415) 555-0199",
      "managerName": "Engineering & Technology"
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageEmployeeSummary`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<EmployeeSummary>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "employeeCode": "EMP-001",
      "firstName": "Emp",
      "lastName": "Loyee",
      "email": "employee@acme.local",
      "departmentId": "0190df0d-dddd-7000-a000-000000000001",
      "departmentName": "Engineering & Technology",
      "designationId": "0190df0d-eeee-7000-a000-000000000001",
      "designationTitle": "Senior Software Engineer",
      "locationId": "0190df0d-cccc-7000-a000-000000000001",
      "locationName": "Bengaluru Headquarters",
      "dateOfJoining": "2026-01-01",
      "employmentStatus": "ACTIVE"
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageLegalEntityResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<LegalEntityResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `PageLocationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<LocationResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "code": "APEX",
      "name": "Apex Innovations LLC",
      "city": "Example Value",
      "country": "Example Value",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

## 04. Employee Directory & Profiles

Contains **2** data models.

### `ProfileChangeRequestResponse`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `status` | `string` | No | - | - |
| `requestedBy` | `string (uuid)` | No | - | - |
| `requestedAt` | `string (date-time)` | No | - | - |
| `approvedBy` | `string (uuid)` | No | - | - |
| `approvedAt` | `string (date-time)` | No | - | - |
| `changeJson` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "status": "ACTIVE",
  "requestedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "requestedAt": "2026-09-21T10:00:00Z",
  "approvedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "approvedAt": "2026-09-21T10:00:00Z",
  "changeJson": "Example Value"
}
```

---

### `ProfileResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `personal` | ``PersonalInfo`` | No | - | - |
| `emergencyContacts` | `Array<EmergencyContactDto>` | No | - | - |
| `dependents` | `Array<DependentDto>` | No | - | - |
| `education` | `Array<EducationDto>` | No | - | - |
| `experience` | `Array<ExperienceDto>` | No | - | - |
| `skills` | `Array<SkillDto>` | No | - | - |
| `certifications` | `Array<CertificationDto>` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "personal": {
    "firstName": "Emp",
    "lastName": "Loyee",
    "preferredName": "Engineering & Technology",
    "email": "employee@acme.local",
    "phoneNumber": "+1 (415) 555-0199",
    "dateOfBirth": "1992-06-15",
    "bankAccountNumber": "Example Value",
    "taxId": "Example Value"
  },
  "emergencyContacts": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "name": "Apex Innovations LLC",
      "relationship": "Example Value",
      "phone": "Example Value"
    }
  ],
  "dependents": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "name": "Apex Innovations LLC",
      "relationship": "Example Value",
      "dateOfBirth": "1992-06-15"
    }
  ],
  "education": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "institution": "Example Value",
      "degree": "Example Value",
      "yearOfPassing": 5000.0
    }
  ],
  "experience": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "companyName": "Engineering & Technology",
      "role": "Example Value",
      "startDate": "2026-02-01",
      "endDate": "2026-11-30"
    }
  ],
  "skills": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "skillName": "Engineering & Technology",
      "proficiency": "Example Value"
    }
  ],
  "certifications": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "certificationName": "Engineering & Technology",
      "issuer": "Example Value",
      "expiryDate": "2026-02-01"
    }
  ]
}
```

---

## 05. Time, Attendance & Timesheets

Contains **3** data models.

### `AttendanceResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `employeeName` | `string` | No | - | - |
| `clockIn` | `string (date-time)` | No | - | - |
| `clockOut` | `string (date-time)` | No | - | - |
| `status` | `string` | No | - | - |
| `workingHours` | `number` | No | - | - |
| `ipAddress` | `string` | No | - | - |
| `notes` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "employeeName": "Emp Loyee",
  "clockIn": "2026-09-21T09:00:00Z",
  "clockOut": "2026-09-21T17:30:00Z",
  "status": "ACTIVE",
  "workingHours": 5000.0,
  "ipAddress": "Example Value",
  "notes": "Completed sprint deliverables on schedule."
}
```

---

### `ClockInRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `ipAddress` | `string` | No | - | - |
| `notes` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "ipAddress": "Example Value",
  "notes": "Completed sprint deliverables on schedule."
}
```

---

### `ClockOutRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `notes` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "notes": "Completed sprint deliverables on schedule."
}
```

---

## 06. Leaves & Absence Management

Contains **3** data models.

### `LeaveBalanceResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `leaveTypeId` | `string (uuid)` | No | - | - |
| `leaveTypeCode` | `string` | No | - | - |
| `leaveTypeName` | `string` | No | - | - |
| `balance` | `number` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "leaveTypeId": "0190df0d-7777-7000-a000-000000000001",
  "leaveTypeCode": "ANNUAL",
  "leaveTypeName": "Engineering & Technology",
  "balance": 18.5
}
```

---

### `LeaveLedgerEntryDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `transactionType` | `string` | No | - | - |
| `quantity` | `number` | No | - | - |
| `effectiveDate` | `string (date)` | No | - | - |
| `sourceReference` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "transactionType": "Example Value",
  "quantity": 1.5,
  "effectiveDate": "2026-02-01",
  "sourceReference": "Example Value",
  "createdAt": "2026-09-21T10:00:00Z"
}
```

---

### `LeaveRequestResponseDto`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `leaveTypeId` | `string (uuid)` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `reason` | `string` | No | - | - |
| `totalDays` | `number` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "leaveTypeId": "0190df0d-7777-7000-a000-000000000001",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "status": "ACTIVE",
  "reason": "Completed sprint deliverables on schedule.",
  "totalDays": 3.0
}
```

---

## 07. Payroll & Compensation

Contains **6** data models.

### `PayrollRunCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `periodStart` | `string (date)` | No | - | - |
| `periodEnd` | `string (date)` | No | - | - |
| `payoutDate` | `string (date)` | No | - | - |
| `runType` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "periodStart": "2026-09-01",
  "periodEnd": "2026-09-30",
  "payoutDate": "2026-09-30",
  "runType": "REGULAR"
}
```

---

### `PayrollRunResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `periodStart` | `string (date)` | No | - | - |
| `periodEnd` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `payoutDate` | `string (date)` | No | - | - |
| `totalGross` | `number` | No | - | - |
| `totalDeductions` | `number` | No | - | - |
| `totalNet` | `number` | No | - | - |
| `runType` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "periodStart": "2026-09-01",
  "periodEnd": "2026-09-30",
  "status": "ACTIVE",
  "payoutDate": "2026-09-30",
  "totalGross": 85000.0,
  "totalDeductions": 17000.0,
  "totalNet": 68000.0,
  "runType": "REGULAR"
}
```

---

### `PayslipItemResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `itemName` | `string` | No | - | - |
| `itemType` | `string` | No | - | - |
| `amount` | `number` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "itemName": "Engineering & Technology",
  "itemType": "Example Value",
  "amount": 85000.0
}
```

---

### `PayslipResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `payrollRunId` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `employeeName` | `string` | No | - | - |
| `basicSalary` | `number` | No | - | - |
| `allowances` | `number` | No | - | - |
| `deductions` | `number` | No | - | - |
| `taxDeductions` | `number` | No | - | - |
| `netSalary` | `number` | No | - | - |
| `workingDays` | `integer (int32)` | No | - | - |
| `presentDays` | `integer (int32)` | No | - | - |
| `leaveDays` | `integer (int32)` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `sentAt` | `string (date-time)` | No | - | - |
| `items` | `Array<PayslipItemResponse>` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "payrollRunId": "0190df0d-8888-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "employeeName": "Emp Loyee",
  "basicSalary": 65000.0,
  "allowances": 20000.0,
  "deductions": 5000.0,
  "taxDeductions": 12000.0,
  "netSalary": 68000.0,
  "workingDays": 22,
  "presentDays": 21,
  "leaveDays": 1,
  "currencyCode": "USD",
  "status": "ACTIVE",
  "sentAt": "2026-09-21T12:00:00Z",
  "items": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "itemName": "Engineering & Technology",
      "itemType": "Example Value",
      "amount": 85000.0
    }
  ]
}
```

---

### `SalaryCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - | - |
| `amount` | `number` | **Yes** | min: 0.0001 | - |
| `currencyCode` | `string` | **Yes** | max 3 chars, min 3 chars | - |
| `effectiveFrom` | `string (date)` | **Yes** | - | - |
| `effectiveTo` | `string (date)` | No | - | - |
| `reason` | `string` | No | max 1024 chars | - |

#### Realistic Business Example JSON
```json
{
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "amount": 85000.0,
  "currencyCode": "USD",
  "effectiveFrom": "2026-02-01",
  "effectiveTo": "2026-02-01",
  "reason": "Completed sprint deliverables on schedule."
}
```

---

### `SalaryResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `amount` | `number` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `effectiveFrom` | `string (date)` | No | - | - |
| `effectiveTo` | `string (date)` | No | - | - |
| `reason` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "amount": 85000.0,
  "currencyCode": "USD",
  "effectiveFrom": "2026-02-01",
  "effectiveTo": "2026-02-01",
  "reason": "Completed sprint deliverables on schedule.",
  "createdAt": "2026-09-21T10:00:00Z"
}
```

---

## 08. Recruitment & Applicant Tracking System (ATS)

Contains **13** data models.

### `CandidateApplicationCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `firstName` | `string` | No | - | - |
| `lastName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `phone` | `string` | No | - | - |
| `resumeStorageKey` | `string` | No | - | - |
| `skills` | `string` | No | - | - |
| `profileSummary` | `string` | No | - | - |
| `coverLetter` | `string` | No | - | - |
| `source` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "firstName": "Emp",
  "lastName": "Loyee",
  "email": "employee@acme.local",
  "phone": "Example Value",
  "resumeStorageKey": "Example Value",
  "skills": "Example Value",
  "profileSummary": "Example Value",
  "coverLetter": "Example Value",
  "source": "Example Value"
}
```

---

### `CandidateApplicationResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `candidateId` | `string (uuid)` | No | - | - |
| `candidateName` | `string` | No | - | - |
| `candidateEmail` | `string` | No | - | - |
| `candidatePhone` | `string` | No | - | - |
| `jobOpeningId` | `string (uuid)` | No | - | - |
| `jobTitle` | `string` | No | - | - |
| `currentStage` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `source` | `string` | No | - | - |
| `resumeStorageKey` | `string` | No | - | - |
| `coverLetter` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateId": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateName": "Engineering & Technology",
  "candidateEmail": "Example Value",
  "candidatePhone": "Example Value",
  "jobOpeningId": "0190df0d-aaaa-7000-a000-000000000001",
  "jobTitle": "Senior Software Engineer",
  "currentStage": "Example Value",
  "status": "ACTIVE",
  "source": "Example Value",
  "resumeStorageKey": "Example Value",
  "coverLetter": "Example Value",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `InterviewFeedbackResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `interviewId` | `string (uuid)` | No | - | - |
| `interviewerId` | `string (uuid)` | No | - | - |
| `interviewerName` | `string` | No | - | - |
| `score` | `integer (int32)` | No | - | - |
| `recommendation` | `string` | No | - | - |
| `comments` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "interviewId": "0190df0d-2222-7000-a000-000000000001",
  "interviewerId": "0190df0d-aaaa-7000-a000-000000000001",
  "interviewerName": "Engineering & Technology",
  "score": 5000.0,
  "recommendation": "Example Value",
  "comments": "Completed sprint deliverables on schedule.",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `InterviewFeedbackSubmitRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `interviewerId` | `string (uuid)` | No | - | - |
| `score` | `integer (int32)` | No | - | - |
| `recommendation` | `string` | No | - | - |
| `comments` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "interviewerId": "0190df0d-aaaa-7000-a000-000000000001",
  "score": 5000.0,
  "recommendation": "Example Value",
  "comments": "Completed sprint deliverables on schedule."
}
```

---

### `InterviewResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `candidateApplicationId` | `string (uuid)` | No | - | - |
| `interviewType` | `string` | No | - | - |
| `scheduledTime` | `string (date-time)` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateApplicationId": "0190df0d-aaaa-7000-a000-000000000001",
  "interviewType": "Example Value",
  "scheduledTime": "2026-09-21T10:00:00Z",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `InterviewScheduleRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `interviewType` | `string` | No | - | - |
| `scheduledTime` | `string (date-time)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "interviewType": "Example Value",
  "scheduledTime": "2026-09-21T10:00:00Z"
}
```

---

### `JobOpeningResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `jobRequisitionId` | `string (uuid)` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "jobRequisitionId": "0190df0d-aaaa-7000-a000-000000000001",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `JobPostingCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `title` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `locationName` | `string` | No | - | - |
| `workArrangement` | `string` | No | - | - |
| `employmentType` | `string` | No | - | - |
| `applicationDeadline` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "title": "Senior Software Engineer",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "locationName": "Bengaluru Headquarters",
  "workArrangement": "Example Value",
  "employmentType": "Example Value",
  "applicationDeadline": "2026-02-01"
}
```

---

### `JobPostingResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `jobOpeningId` | `string (uuid)` | No | - | - |
| `publicId` | `string (uuid)` | No | - | - |
| `title` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `locationName` | `string` | No | - | - |
| `workArrangement` | `string` | No | - | - |
| `employmentType` | `string` | No | - | - |
| `applicationDeadline` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "jobOpeningId": "0190df0d-aaaa-7000-a000-000000000001",
  "publicId": "0190df0d-aaaa-7000-a000-000000000001",
  "title": "Senior Software Engineer",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "locationName": "Bengaluru Headquarters",
  "workArrangement": "Example Value",
  "employmentType": "Example Value",
  "applicationDeadline": "2026-02-01",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `JobRequisitionCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `jobTitle` | `string` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `legalEntityId` | `string (uuid)` | No | - | - |
| `employmentType` | `string` | No | - | - |
| `openingsCount` | `integer (int32)` | No | - | - |
| `hiringManagerId` | `string (uuid)` | No | - | - |
| `targetStartDate` | `string (date)` | No | - | - |
| `minSalary` | `number` | No | - | - |
| `maxSalary` | `number` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `requiredSkills` | `string` | No | - | - |
| `minExperienceYears` | `integer (int32)` | No | - | - |
| `description` | `string` | No | - | - |
| `justification` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "jobTitle": "Senior Software Engineer",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "legalEntityId": "0190df0d-bbbb-7000-a000-000000000001",
  "employmentType": "Example Value",
  "openingsCount": 10,
  "hiringManagerId": "0190df0d-aaaa-7000-a000-000000000001",
  "targetStartDate": "2026-02-01",
  "minSalary": 5000.0,
  "maxSalary": 5000.0,
  "currencyCode": "USD",
  "requiredSkills": "Example Value",
  "minExperienceYears": 5000.0,
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "justification": "Example Value"
}
```

---

### `JobRequisitionResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `jobOpeningId` | `string (uuid)` | No | - | - |
| `reqNumber` | `string` | No | - | - |
| `jobTitle` | `string` | No | - | - |
| `departmentId` | `string (uuid)` | No | - | - |
| `departmentName` | `string` | No | - | - |
| `designationId` | `string (uuid)` | No | - | - |
| `designationTitle` | `string` | No | - | - |
| `locationId` | `string (uuid)` | No | - | - |
| `locationName` | `string` | No | - | - |
| `legalEntityId` | `string (uuid)` | No | - | - |
| `legalEntityName` | `string` | No | - | - |
| `employmentType` | `string` | No | - | - |
| `openingsCount` | `integer (int32)` | No | - | - |
| `hiringManagerId` | `string (uuid)` | No | - | - |
| `hiringManagerName` | `string` | No | - | - |
| `recruiterId` | `string (uuid)` | No | - | - |
| `recruiterName` | `string` | No | - | - |
| `targetStartDate` | `string (date)` | No | - | - |
| `minSalary` | `number` | No | - | - |
| `maxSalary` | `number` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `requiredSkills` | `string` | No | - | - |
| `minExperienceYears` | `integer (int32)` | No | - | - |
| `description` | `string` | No | - | - |
| `justification` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "jobOpeningId": "0190df0d-aaaa-7000-a000-000000000001",
  "reqNumber": "Example Value",
  "jobTitle": "Senior Software Engineer",
  "departmentId": "0190df0d-dddd-7000-a000-000000000001",
  "departmentName": "Engineering & Technology",
  "designationId": "0190df0d-eeee-7000-a000-000000000001",
  "designationTitle": "Senior Software Engineer",
  "locationId": "0190df0d-cccc-7000-a000-000000000001",
  "locationName": "Bengaluru Headquarters",
  "legalEntityId": "0190df0d-bbbb-7000-a000-000000000001",
  "legalEntityName": "Acme Corporation Global Inc.",
  "employmentType": "Example Value",
  "openingsCount": 10,
  "hiringManagerId": "0190df0d-aaaa-7000-a000-000000000001",
  "hiringManagerName": "Engineering & Technology",
  "recruiterId": "0190df0d-aaaa-7000-a000-000000000001",
  "recruiterName": "Engineering & Technology",
  "targetStartDate": "2026-02-01",
  "minSalary": 5000.0,
  "maxSalary": 5000.0,
  "currencyCode": "USD",
  "requiredSkills": "Example Value",
  "minExperienceYears": 5000.0,
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "justification": "Example Value",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `OfferCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `salaryAmount` | `number` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "salaryAmount": 5000.0,
  "currencyCode": "USD",
  "startDate": "2026-02-01"
}
```

---

### `OfferResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `candidateApplicationId` | `string (uuid)` | No | - | - |
| `candidateName` | `string` | No | - | - |
| `candidateEmail` | `string` | No | - | - |
| `jobTitle` | `string` | No | - | - |
| `salaryAmount` | `number` | No | - | - |
| `currencyCode` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `secureToken` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateApplicationId": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateName": "Engineering & Technology",
  "candidateEmail": "Example Value",
  "jobTitle": "Senior Software Engineer",
  "salaryAmount": 5000.0,
  "currencyCode": "USD",
  "startDate": "2026-02-01",
  "status": "ACTIVE",
  "secureToken": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

## 09. Pre-Hire Onboarding & Candidate Activation

Contains **5** data models.

### `AssetRequestResponse`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `preHireId` | `string (uuid)` | No | - | - |
| `assetType` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "preHireId": "0190df0d-aaaa-7000-a000-000000000001",
  "assetType": "Example Value",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `BackgroundCheckResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `preHireId` | `string (uuid)` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "preHireId": "0190df0d-aaaa-7000-a000-000000000001",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `OnboardingPlanResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `preHireId` | `string (uuid)` | No | - | - |
| `templateName` | `string` | No | - | - |
| `tasks` | `Array<OnboardingTaskResponse>` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "preHireId": "0190df0d-aaaa-7000-a000-000000000001",
  "templateName": "Engineering & Technology",
  "tasks": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "onboardingPlanId": "0190df0d-aaaa-7000-a000-000000000001",
      "taskName": "Engineering & Technology",
      "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
      "assignedRole": "Example Value",
      "status": "ACTIVE",
      "dueDate": "2026-02-01",
      "createdAt": "2026-09-21T10:00:00Z",
      "updatedAt": "2026-09-21T10:00:00Z",
      "version": 0
    }
  ],
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `OnboardingTaskResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `onboardingPlanId` | `string (uuid)` | No | - | - |
| `taskName` | `string` | No | - | - |
| `description` | `string` | No | - | - |
| `assignedRole` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `dueDate` | `string (date)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "onboardingPlanId": "0190df0d-aaaa-7000-a000-000000000001",
  "taskName": "Engineering & Technology",
  "description": "Enterprise cloud analytics and real-time distributed telemetry platform.",
  "assignedRole": "Example Value",
  "status": "ACTIVE",
  "dueDate": "2026-02-01",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `PreHireResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `candidateId` | `string (uuid)` | No | - | - |
| `candidateName` | `string` | No | - | - |
| `candidateEmail` | `string` | No | - | - |
| `acceptedOfferId` | `string (uuid)` | No | - | - |
| `legalEntityName` | `string` | No | - | - |
| `departmentName` | `string` | No | - | - |
| `designationTitle` | `string` | No | - | - |
| `locationName` | `string` | No | - | - |
| `managerName` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateId": "0190df0d-aaaa-7000-a000-000000000001",
  "candidateName": "Engineering & Technology",
  "candidateEmail": "Example Value",
  "acceptedOfferId": "0190df0d-aaaa-7000-a000-000000000001",
  "legalEntityName": "Acme Corporation Global Inc.",
  "departmentName": "Engineering & Technology",
  "designationTitle": "Senior Software Engineer",
  "locationName": "Bengaluru Headquarters",
  "managerName": "Engineering & Technology",
  "startDate": "2026-02-01",
  "status": "ACTIVE",
  "createdAt": "2026-09-21T10:00:00Z",
  "updatedAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

## 11. Announcements & Workflow Approvals

Contains **3** data models.

### `ApprovalRequestCreateRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - | - |
| `type` | `string` | **Yes** | min 1 chars | - |
| `changeJson` | `string` | **Yes** | min 1 chars | - |

#### Realistic Business Example JSON
```json
{
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "type": "Example Value",
  "changeJson": "Example Value"
}
```

---

### `ApprovalRequestResponse`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `requesterId` | `string (uuid)` | No | - | - |
| `employeeId` | `string (uuid)` | No | - | - |
| `type` | `string` | No | - | - |
| `changeJson` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `approvedBy` | `string (uuid)` | No | - | - |
| `approvedAt` | `string (date-time)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "requesterId": "0190df0d-aaaa-7000-a000-000000000001",
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "type": "Example Value",
  "changeJson": "Example Value",
  "status": "ACTIVE",
  "approvedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "approvedAt": "2026-09-21T10:00:00Z",
  "createdAt": "2026-09-21T10:00:00Z",
  "version": 0
}
```

---

### `WorkflowDefinition`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `workflowType` | `string` | No | - | - |
| `name` | `string` | No | - | - |
| `rulesJson` | `string` | No | - | - |
| `status` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "workflowType": "Example Value",
  "name": "Apex Innovations LLC",
  "rulesJson": "Example Value",
  "status": "ACTIVE",
  "deleted": true
}
```

---

## 13. Common & Infrastructure DTOs

Contains **17** data models.

### `AuditLogEntryResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `at` | `string (date-time)` | No | - | - |
| `actorId` | `string (uuid)` | No | - | - |
| `actorLabel` | `string` | No | - | - |
| `action` | `string` | No | - | - |
| `entity` | `string` | No | - | - |
| `entityId` | `string (uuid)` | No | - | - |
| `requestId` | `string` | No | - | - |
| `detail` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "at": "2026-09-21T10:00:00Z",
  "actorId": "0190df0d-aaaa-7000-a000-000000000001",
  "actorLabel": "Example Value",
  "action": "Example Value",
  "entity": "Example Value",
  "entityId": "0190df0d-aaaa-7000-a000-000000000001",
  "requestId": "Example Value",
  "detail": "Example Value"
}
```

---

### `CertificationDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `certificationName` | `string` | No | - | - |
| `issuer` | `string` | No | - | - |
| `expiryDate` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "certificationName": "Engineering & Technology",
  "issuer": "Example Value",
  "expiryDate": "2026-02-01"
}
```

---

### `CompleteUploadRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `sizeBytes` | `integer (int64)` | **Yes** | - | - |

#### Realistic Business Example JSON
```json
{
  "sizeBytes": 5000.0
}
```

---

### `DependentDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `name` | `string` | No | - | - |
| `relationship` | `string` | No | - | - |
| `dateOfBirth` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "name": "Apex Innovations LLC",
  "relationship": "Example Value",
  "dateOfBirth": "1992-06-15"
}
```

---

### `EducationDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `institution` | `string` | No | - | - |
| `degree` | `string` | No | - | - |
| `yearOfPassing` | `integer (int32)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "institution": "Example Value",
  "degree": "Example Value",
  "yearOfPassing": 5000.0
}
```

---

### `ExperienceDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `companyName` | `string` | No | - | - |
| `role` | `string` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "companyName": "Engineering & Technology",
  "role": "Example Value",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30"
}
```

---

### `HrOverviewResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `headcountActive` | `integer (int64)` | No | - | - |
| `joinersLast30Days` | `integer (int64)` | No | - | - |
| `leaversLast30Days` | `integer (int64)` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "headcountActive": 10,
  "joinersLast30Days": 10,
  "leaversLast30Days": 10
}
```

---

### `ManagerDelegation`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `createdAt` | `string (date-time)` | No | - | - |
| `createdBy` | `string (uuid)` | No | - | - |
| `updatedAt` | `string (date-time)` | No | - | - |
| `updatedBy` | `string (uuid)` | No | - | - |
| `tenantId` | `string (uuid)` | No | - | - |
| `deletedAt` | `string (date-time)` | No | - | - |
| `version` | `integer (int64)` | No | - | - |
| `managerId` | `string (uuid)` | No | - | - |
| `delegateId` | `string (uuid)` | No | - | - |
| `startDate` | `string (date)` | No | - | - |
| `endDate` | `string (date)` | No | - | - |
| `status` | `string` | No | - | - |
| `deleted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "createdAt": "2026-09-21T10:00:00Z",
  "createdBy": "0190df0d-aaaa-7000-a000-000000000001",
  "updatedAt": "2026-09-21T10:00:00Z",
  "updatedBy": "0190df0d-aaaa-7000-a000-000000000001",
  "tenantId": "0190df0d-aaaa-7000-a000-000000000001",
  "deletedAt": "2026-09-21T10:00:00Z",
  "version": 0,
  "managerId": "0190df0d-ffff-7c00-a000-000000000001",
  "delegateId": "0190df0d-aaaa-7000-a000-000000000001",
  "startDate": "2026-02-01",
  "endDate": "2026-11-30",
  "status": "ACTIVE",
  "deleted": true
}
```

---

### `PageAuditLogEntryResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `totalPages` | `integer (int32)` | No | - | - |
| `totalElements` | `integer (int64)` | No | - | - |
| `first` | `boolean` | No | - | - |
| `last` | `boolean` | No | - | - |
| `size` | `integer (int32)` | No | - | - |
| `content` | `Array<AuditLogEntryResponse>` | No | - | - |
| `number` | `integer (int32)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `pageable` | ``PageableObject`` | No | - | - |
| `numberOfElements` | `integer (int32)` | No | - | - |
| `empty` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "totalPages": 10,
  "totalElements": 10,
  "first": true,
  "last": true,
  "size": 5000.0,
  "content": [
    {
      "id": "0190df0d-aaaa-7000-a000-000000000001",
      "at": "2026-09-21T10:00:00Z",
      "actorId": "0190df0d-aaaa-7000-a000-000000000001",
      "actorLabel": "Example Value",
      "action": "Example Value",
      "entity": "Example Value",
      "entityId": "0190df0d-aaaa-7000-a000-000000000001",
      "requestId": "Example Value",
      "detail": "Example Value"
    }
  ],
  "number": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "pageable": {
    "offset": 5000.0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    },
    "paged": true,
    "pageNumber": 5000.0,
    "pageSize": 5000.0,
    "unpaged": true
  },
  "numberOfElements": 10,
  "empty": true
}
```

---

### `Pageable`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `page` | `integer (int32)` | No | min: 0 | - |
| `size` | `integer (int32)` | No | min: 1 | - |
| `sort` | `Array<string>` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "page": 5000.0,
  "size": 5000.0,
  "sort": [
    "Item 1",
    "Item 2"
  ]
}
```

---

### `PageableObject`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `offset` | `integer (int64)` | No | - | - |
| `sort` | ``SortObject`` | No | - | - |
| `paged` | `boolean` | No | - | - |
| `pageNumber` | `integer (int32)` | No | - | - |
| `pageSize` | `integer (int32)` | No | - | - |
| `unpaged` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "offset": 5000.0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "paged": true,
  "pageNumber": 5000.0,
  "pageSize": 5000.0,
  "unpaged": true
}
```

---

### `PersonalInfo`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `firstName` | `string` | No | - | - |
| `lastName` | `string` | No | - | - |
| `preferredName` | `string` | No | - | - |
| `email` | `string` | No | - | - |
| `phoneNumber` | `string` | No | - | - |
| `dateOfBirth` | `string (date)` | No | - | - |
| `bankAccountNumber` | `string` | No | - | - |
| `taxId` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "firstName": "Emp",
  "lastName": "Loyee",
  "preferredName": "Engineering & Technology",
  "email": "employee@acme.local",
  "phoneNumber": "+1 (415) 555-0199",
  "dateOfBirth": "1992-06-15",
  "bankAccountNumber": "Example Value",
  "taxId": "Example Value"
}
```

---

### `PresignDownloadResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `string (uuid)` | No | - | - |
| `downloadUrl` | `string` | No | - | - |
| `httpMethod` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "documentId": "0190df0d-aaaa-7000-a000-000000000001",
  "downloadUrl": "Example Value",
  "httpMethod": "Example Value"
}
```

---

### `PresignUploadRequest`
**Classification**: `HTTP Request Body`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `employeeId` | `string (uuid)` | **Yes** | - | - |
| `documentType` | `string` | **Yes** | Values: `[PROFILE_PHOTO, OFFER_LETTER, ID_PROOF, OTHER]` | - |
| `contentType` | `string` | **Yes** | max 255 chars | - |
| `originalFilename` | `string` | **Yes** | max 512 chars | - |
| `sharable` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "employeeId": "0190df0d-ffff-7c00-a000-000000000003",
  "documentType": "PROFILE_PHOTO",
  "contentType": "Example Value",
  "originalFilename": "Engineering & Technology",
  "sharable": true
}
```

---

### `PresignUploadResponse`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `documentId` | `string (uuid)` | No | - | - |
| `uploadUrl` | `string` | No | - | - |
| `httpMethod` | `string` | No | - | - |
| `storageKey` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "documentId": "0190df0d-aaaa-7000-a000-000000000001",
  "uploadUrl": "Example Value",
  "httpMethod": "Example Value",
  "storageKey": "Example Value"
}
```

---

### `SkillDto`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `string (uuid)` | No | - | - |
| `skillName` | `string` | No | - | - |
| `proficiency` | `string` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "id": "0190df0d-aaaa-7000-a000-000000000001",
  "skillName": "Engineering & Technology",
  "proficiency": "Example Value"
}
```

---

### `SortObject`
**Classification**: `HTTP Response Payload`  

#### Field Definitions & Validation Constraints
| Field Name | Type | Required | Constraints / Rules | Description |
| :--- | :--- | :--- | :--- | :--- |
| `empty` | `boolean` | No | - | - |
| `sorted` | `boolean` | No | - | - |
| `unsorted` | `boolean` | No | - | - |

#### Realistic Business Example JSON
```json
{
  "empty": true,
  "sorted": true,
  "unsorted": true
}
```

---
