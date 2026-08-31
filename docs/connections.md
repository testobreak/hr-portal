# HRMS — Local Environment Connections & Credentials Reference

This document provides a single reference point for all URLs, ports, service endpoints, administrative credentials, database connection parameters, and seeded application accounts across the **HRMS** platform.

---

## 1. Platform Infrastructure Services (Docker Containers)

These services are orchestrated via [`infra/docker-compose.yml`](file:///d:/HRMS/infra/docker-compose.yml) and configured via [`.env`](file:///d:/HRMS/.env).

| Service | Access URL / Host | Container Name | Username / User | Password | Details |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL 18** | `localhost:55432` | `hrms-postgres` | `hrms` | `hrms_local_dev_change_me` | DB: `hrms` \| JDBC: `jdbc:postgresql://localhost:55432/hrms` |
| **Keycloak 26 Admin**| `http://localhost:8081` | `hrms-keycloak` | `admin` | `admin_local_dev_change_me` | Master Realm Admin Console |
| **MinIO Console** | `http://localhost:59001` | `hrms-minio` | `hrms_minio_admin` | `hrms_minio_local_change_me` | Object Storage Web Console |
| **MinIO S3 API** | `http://localhost:59000` | `hrms-minio` | `hrms_minio_admin` | `hrms_minio_local_change_me` | S3 Storage Bucket: `hrms-documents` |
| **Redis Cache** | `localhost:6379` | `hrms-redis` | *(None)* | *(No auth in dev)* | Cache store for metrics & sessions |

---

## 2. Application Services (Host Environment)

| Application | URL | Launch Command | Health / API Docs |
| :--- | :--- | :--- | :--- |
| **Spring Boot Backend** | `http://localhost:8080` | `.\run-backend.ps1` | Health: `http://localhost:8080/actuator/health`<br>Swagger: `http://localhost:8080/swagger-ui.html` |
| **React Frontend (Vite)** | `http://localhost:5173` | `cd frontend; npm run dev` | Login Page: `http://localhost:5173/login` |

---

## 3. Seeded Application Accounts (HRMS Login)

These user accounts are seeded in the PostgreSQL `employee` table for logging into the Frontend UI (`http://localhost:5173/login`):

| Role / Intent | Email (Username) | Password | Assigned Roles |
| :--- | :--- | :--- | :--- |
| **Super Admin / Org Admin** | `admin@acme.local` | `Admin#12345` | `SUPER_ADMIN`, `ORGANIZATION_ADMIN`, `HR_ADMIN`, `FINANCE_ADMIN` |
| **HR Admin / HR Executive** | `hr@acme.local` | `Hr#1234567` | `HR_ADMIN`, `HR_EXECUTIVE` |
| **Employee** | `employee@acme.local` | `Emp#1234567` | `EMPLOYEE` |

> **Note:** Password input on the login form is **case-sensitive** (e.g. capital `A` in `Admin#12345`, capital `H` in `Hr#1234567`).

---

## 4. Database Access Quick Reference

### A. Terminal Command (Zero Installation Required)
Connect directly to PostgreSQL inside the Docker container without installing PostgreSQL on Windows:

```powershell
docker exec -it hrms-postgres psql -U hrms -d hrms
```

### B. GUI Database Client Connection Parameters (DBeaver, DataGrip, pgAdmin)
- **Host:** `localhost`
- **Port:** `55432`
- **Database:** `hrms`
- **User:** `hrms`
- **Password:** `hrms_local_dev_change_me`

### C. Useful One-Liner PowerShell Queries
```powershell
# View all seeded employees
docker exec hrms-postgres psql -U hrms -d hrms -c "SELECT id, email, employee_code FROM employee;"

# View assigned roles per user
docker exec hrms-postgres psql -U hrms -d hrms -c "SELECT e.email, er.role FROM employee e JOIN employee_role er ON e.id = er.employee_id;"

# View Flyway database migration status
docker exec hrms-postgres psql -U hrms -d hrms -c "SELECT version, description, installed_on FROM flyway_schema_history;"
```
