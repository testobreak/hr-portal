import { get } from "../client/apiClient";

describe("Salary API", () => {
  test("GET /api/salaries/me matches OpenAPI contract", async () => {
    const response = await get("/api/salaries/me");

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(Array.isArray(response.body)).toBe(true);
  });

  test("GET /api/salaries/employee/{employeeId} matches OpenAPI contract", async () => {
    // Look up an employee first
    const empResponse = await get("/api/employees");
    const employees = empResponse.body?.content || (Array.isArray(empResponse.body) ? empResponse.body : []);

    if (employees.length === 0) {
      console.warn("Skipping GET /api/salaries/employee/{id}: No employees found");
      return;
    }

    const employeeId = employees[0].id;
    const response = await get(`/api/salaries/employee/${employeeId}`);

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(Array.isArray(response.body)).toBe(true);
  });
});
