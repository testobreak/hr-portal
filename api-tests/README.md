# HRMS API Contract Validation Suite

Independent TypeScript API validation and contract test suite for the HRMS backend.

## Overview
- **Runner**: Jest + `ts-jest`
- **HTTP Client**: Supertest
- **Contract Validation**: `jest-openapi` against `openapi.yaml` (`expect(response).toSatisfyApiSpec()`)
- **Custom Schema Validation**: `Ajv` (for standalone JSON schemas)
- **Zero Internal Coupling**: Does not rely on Spring Boot test harness, Testcontainers, DB cleanup scripts, or MockMvc.

## Directory Structure
```
api-tests/
├── package.json
├── tsconfig.json
├── jest.config.ts
├── src/
│   ├── config/
│   │   └── env.ts
│   ├── client/
│   │   └── apiClient.ts
│   ├── utils/
│   │   ├── schemaValidator.ts
│   │   └── openApiLoader.ts
│   └── tests/
│       ├── employee.api.test.ts
│       ├── department.api.test.ts
│       ├── designation.api.test.ts
│       ├── project.api.test.ts
│       └── salary.api.test.ts
└── schemas/
    └── optional-custom-schemas/
        └── department.schema.json
```

## Running Locally

1. **Install dependencies**:
   ```bash
   cd api-tests
   npm install
   ```

2. **Ensure HRMS Backend is running**:
   - By default tests target `http://localhost:8080`.
   - Automated authentication logs in via `POST /api/v1/auth/login` using seeded credentials (`admin@acme.local` / `Admin#12345`).

3. **Run tests**:
   ```bash
   npm test
   ```

4. **Run against an external / deployed environment**:
   ```bash
   API_BASE_URL=https://staging-api.acme.local API_TOKEN=your_jwt_token npm test
   ```

## CI / GitHub Actions
Automated contract tests run on PRs to `main`, pushes to `main`, and manual `workflow_dispatch`:
- `.github/workflows/api-tests.yml`

## Commands Cheatsheet
For detailed commands to run single tests, specific test cases, watch mode, coverage, and custom flags, see the [API Testing Cheatsheet](../docs/api-testing-cheatsheet.md).

