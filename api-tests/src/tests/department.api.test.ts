import { get } from "../client/apiClient";
import { validateSchema } from "../utils/schemaValidator";
import departmentSchema from "../../schemas/optional-custom-schemas/department.schema.json";

describe("Department API", () => {
  let departmentId: string | undefined;

  test("GET /api/departments matches OpenAPI contract and returns collection", async () => {
    const response = await get("/api/departments");

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion (OpenAPI specification)
    expect(response).toSatisfyApiSpec();

    // 3. Business assertions
    expect(response.body).toBeDefined();
    const items = response.body.content || (Array.isArray(response.body) ? response.body : []);
    expect(Array.isArray(items)).toBe(true);

    if (items.length > 0) {
      departmentId = items[0].id;
      expect(items[0]).toHaveProperty("name");
    }
  });

  test("GET /api/departments/{id} matches OpenAPI contract and custom JSON schema", async () => {
    if (!departmentId) {
      console.warn("Skipping GET /api/departments/{id}: No departments available from list endpoint");
      return;
    }

    const response = await get(`/api/departments/${departmentId}`);

    // 1. Transport assertion
    expect(response.status).toBe(200);

    // 2. Contract assertion (OpenAPI specification)
    expect(response).toSatisfyApiSpec();

    // 3. Custom schema validation (optional standalone schema via Ajv)
    const customValidation = validateSchema(departmentSchema, response.body);
    expect(customValidation.valid).toBe(true);

    // 4. Business assertions
    expect(response.body.id).toBe(departmentId);
    expect(response.body.name).toBeDefined();
  });
});
