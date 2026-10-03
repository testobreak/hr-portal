# API Contract & Validation Testing Cheatsheet

This cheatsheet covers all commands to run, filter, debug, and configure the independent TypeScript API testing framework (`api-tests/`), as well as commands for the backend integration tests.

---

## 1. Running From the Repo Root vs `api-tests/`

You can run commands from either location:

### Option A: From inside `api-tests/` (Recommended)
```bash
cd api-tests
npm test
```

### Option B: From the Project Root
```bash
npm --prefix api-tests test
```

---

## 2. Running All Tests at Once

| Command | Description |
| :--- | :--- |
| `npm test` | Runs all 5 test suites sequentially (`--runInBand`) |
| `npx jest` | Runs all test suites with default Jest parallel execution |
| `npm run test:coverage` | Runs all tests and outputs coverage statistics |

---

## 3. Running a Single Specific Test Suite (by File)

To run only one file, pass its filename or relative path:

```bash
# Run Employee API tests only
npx jest src/tests/employee.api.test.ts

# Run Department API tests only
npx jest src/tests/department.api.test.ts

# Run Designation API tests only
npx jest src/tests/designation.api.test.ts

# Run Project API tests only
npx jest src/tests/project.api.test.ts

# Run Salary API tests only
npx jest src/tests/salary.api.test.ts
```

*Shorthand syntax (fuzzy match on filename)*:
```bash
npx jest employee
npx jest department
npx jest designation
npx jest project
npx jest salary
```

---

## 4. Running a Specific Test Case (by Name)

Use the `-t` (or `--testNamePattern`) flag with a regex matching the `test("...")` description:

```bash
# Run only the test that reads an employee by ID
npx jest -t "GET /api/employees/{id}"

# Run only collection list tests across all suites
npx jest -t "returns collection"

# Run only salary history for current user
npx jest -t "GET /api/salaries/me"

# Target a specific test within a specific file
npx jest src/tests/employee.api.test.ts -t "GET /api/employees/{id}"
```

---

## 5. Development & Debugging Modes

### Watch Mode (Re-runs automatically on file change)
```bash
npm run test:watch
# or:
npx jest --watch
```

### Watch a Specific File
```bash
npx jest src/tests/employee.api.test.ts --watch
```

### Verbose Output (Prints full hierarchy and timing of every test)
```bash
npx jest --verbose
```

### Print Detailed Console Logs / Disable Silent Mode
```bash
npx jest --verbose --runInBand --detectOpenHandles
```

### Bail on First Failure (Fail-fast)
```bash
npx jest --bail
```

---

## 6. Running Against Different Environments (Custom Config)

By default, tests run against `http://localhost:8080` and log in with seeded credentials (`admin@acme.local` / `Admin#12345`). You can override any setting using inline environment variables:

### Run Against a Custom Port or Host
```bash
API_BASE_URL=http://localhost:9090 npm test
```

### Run Against Staging or Remote Server
```bash
API_BASE_URL=https://staging-api.acme.local npm test
```

### Run with an Explicit Bearer JWT Token (Bypasses login step)
```bash
API_BASE_URL=https://staging-api.acme.local API_TOKEN=eyJhbGciOi... npm test
```

### Run as a Specific User (Custom Credentials)
```bash
API_EMAIL=hr@acme.local API_PASSWORD=Hr#1234567 npm test
```

### Run with a Custom OpenAPI Spec Location
```bash
OPENAPI_SPEC_PATH=/path/to/custom-openapi.yaml npm test
```

---

## 7. Useful NPM Scripts Reference

Inside [`api-tests/package.json`](file:///Users/ashish/Downloads/hr-portal-main/api-tests/package.json):

```bash
# Standard test execution
npm test

# Watch mode for interactive development
npm run test:watch

# Code coverage report
npm run test:coverage

# Type-check TypeScript files without executing tests
npx tsc --noEmit
```

---

## 8. Bonus: Running Backend Spring Integration Tests

For running backend JUnit / Spring Boot integration tests from [`backend/`](file:///Users/ashish/Downloads/hr-portal-main/backend):

```bash
cd backend

# Run ALL backend tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=EmployeeControllerTest

# Run a single test method within a test class
./mvnw test -Dtest=EmployeeControllerTest#hrCreatesEmployee

# Run all Controller tests
./mvnw test -Dtest=*ControllerTest

# Clean compile before testing
./mvnw clean test-compile
```
