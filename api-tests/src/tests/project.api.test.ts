import { get } from "../client/apiClient";

describe("Project API", () => {
  let projectId: string | undefined;

  test("GET /api/projects matches OpenAPI contract and returns collection", async () => {
    const response = await get("/api/projects");

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body).toBeDefined();
    const items = response.body.content || (Array.isArray(response.body) ? response.body : []);
    expect(Array.isArray(items)).toBe(true);

    if (items.length > 0) {
      projectId = items[0].id;
      expect(items[0]).toHaveProperty("name");
    }
  });

  test("GET /api/projects/{id} matches OpenAPI contract for existing record", async () => {
    if (!projectId) {
      console.warn("Skipping GET /api/projects/{id}: No projects available from list endpoint");
      return;
    }

    const response = await get(`/api/projects/${projectId}`);

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body.id).toBe(projectId);
    expect(response.body.name).toBeDefined();
  });
});
