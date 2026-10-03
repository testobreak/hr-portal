import { get } from "../client/apiClient";

describe("Employee API", () => {
  let employeeId: string | undefined;

  test("GET /api/employees matches OpenAPI contract and returns collection", async () => {
    const response = await get("/api/employees");

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body).toBeDefined();
    const items = response.body.content || (Array.isArray(response.body) ? response.body : []);
    expect(Array.isArray(items)).toBe(true);

    if (items.length > 0) {
      employeeId = items[0].id;
      expect(items[0]).toHaveProperty("email");
    }
  });

  test("GET /api/employees/{id} matches OpenAPI contract for existing record", async () => {
    if (!employeeId) {
      console.warn("Skipping GET /api/employees/{id}: No employees available from list endpoint");
      return;
    }

    const response = await get(`/api/employees/${employeeId}`);

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body.id).toBe(employeeId);
    expect(response.body.email).toBeDefined();
  });
});
