import jestOpenAPI from "jest-openapi";
import path from "path";
import fs from "fs";
import { config } from "../config/env";

const candidatePaths = [
  config.openApiSpecPath,
  path.resolve(__dirname, "../../../openapi.yaml"),
  path.resolve(__dirname, "../../../hrms-openapi.json"),
  path.resolve(process.cwd(), "openapi.yaml"),
  path.resolve(process.cwd(), "hrms-openapi.json"),
  path.resolve(process.cwd(), "../openapi.yaml"),
  path.resolve(process.cwd(), "../hrms-openapi.json"),
];

const resolvedPath = candidatePaths.find((p) => p && fs.existsSync(p));

if (!resolvedPath) {
  throw new Error(
    `OpenAPI spec file could not be found. Checked candidate paths:\n${candidatePaths.filter(Boolean).join("\n")}`
  );
}

// Register jest-openapi matchers (e.g. expect(response).toSatisfyApiSpec())
jestOpenAPI(resolvedPath);

export { resolvedPath };
