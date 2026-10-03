import { get } from "../client/apiClient";

describe("Designation API", () => {
  let designationId: string | undefined;

  test("GET /api/designations matches OpenAPI contract and returns collection", async () => {
    const response = await get("/api/designations");

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body).toBeDefined();
    const items = response.body.content || (Array.isArray(response.body) ? response.body : []);
    expect(Array.isArray(items)).toBe(true);

    if (items.length > 0) {
      designationId = items[0].id;
      expect(items[0]).toHaveProperty("title");
    }
  });

  test("GET /api/designations/{id} matches OpenAPI contract for existing record", async () => {
    if (!designationId) {
      console.warn("Skipping GET /api/designations/{id}: No designations available from list endpoint");
      return;
    }

    const response = await get(`/api/designations/${designationId}`);

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body.id).toBe(designationId);
    expect(response.body.title).toBeDefined();
  });
});
