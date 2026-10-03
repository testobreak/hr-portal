import type { Config } from "jest";

const config: Config = {
  preset: "ts-jest",
  testEnvironment: "node",
  testMatch: ["**/src/tests/**/*.test.ts"],
  setupFilesAfterEnv: ["<rootDir>/src/utils/openApiLoader.ts"],
  testTimeout: 30000,
  verbose: true,
};

export default config;
