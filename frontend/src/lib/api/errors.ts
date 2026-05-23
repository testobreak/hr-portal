export type ProblemDetail = {
  type?: string;
  title?: string;
  status?: number;
  code?: string;
  detail?: string;
  instance?: string;
  traceId?: string;
  fieldErrors?: Array<{ field: string; code: string; message?: string }>;
};

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail | null;
  readonly path: string;

  constructor(status: number, problem: ProblemDetail | null, path: string) {
    const title = problem?.detail ?? problem?.title ?? `Request failed (${status})`;
    super(title);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
    this.path = path;
  }
}
