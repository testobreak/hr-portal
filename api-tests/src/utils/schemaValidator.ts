import Ajv, { AnySchema, ValidateFunction, ErrorObject } from "ajv";
import addFormats from "ajv-formats";

const ajv = new Ajv({
  allErrors: true,
  strict: false,
});
addFormats(ajv);

export interface ValidationResult {
  valid: boolean;
  errors: ErrorObject[] | null | undefined;
}

export function compileSchema(schema: AnySchema): ValidateFunction {
  return ajv.compile(schema);
}

export function validateSchema(schema: AnySchema, data: unknown): ValidationResult {
  const validate = ajv.compile(schema);
  const valid = validate(data);
  return {
    valid: Boolean(valid),
    errors: validate.errors,
  };
}

export { ajv };
