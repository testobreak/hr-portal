import dotenv from "dotenv";
import path from "path";

// Load environment variables from api-tests/.env, root .env, or process.env
dotenv.config({ path: path.resolve(__dirname, "../../.env") });
dotenv.config({ path: path.resolve(__dirname, "../../../.env") });

export const config = {
  apiBaseUrl: process.env.API_BASE_URL || "http://localhost:8080",
  apiToken: process.env.API_TOKEN || "",
  apiEmail: process.env.API_EMAIL || "admin@acme.local",
  apiPassword: process.env.API_PASSWORD || "Admin#12345",
  openApiSpecPath: process.env.OPENAPI_SPEC_PATH || "",
};
