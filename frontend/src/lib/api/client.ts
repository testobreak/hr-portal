import { apiBase } from '@/api/apiConfig';
import { getAccessToken } from '@/auth/keycloak';
import { ApiError, type ProblemDetail } from './errors';

type RequestOptions = {
  body?: unknown;
  headers?: Record<string, string>;
  signal?: AbortSignal;
};

async function parseProblem(res: Response): Promise<ProblemDetail | null> {
  const contentType = res.headers.get('content-type') ?? '';
  if (!contentType.includes('json')) {
    return null;
  }
  try {
    return (await res.json()) as ProblemDetail;
  } catch {
    return null;
  }
}

async function request<T>(method: string, path: string, options: RequestOptions = {}): Promise<T> {
  const doFetch = async (token: string) => {
    const headers: Record<string, string> = {
      Accept: 'application/json',
      Authorization: `Bearer ${token}`,
      ...options.headers,
    };

    let body: string | undefined;
    if (options.body !== undefined) {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(options.body);
    }

    return fetch(`${apiBase}${path}`, {
      method,
      headers,
      body,
      signal: options.signal,
    });
  };

  let token = await getAccessToken();
  let res = await doFetch(token);

  if (res.status === 401) {
    token = await getAccessToken();
    res = await doFetch(token);
  }

  if (!res.ok) {
    const problem = await parseProblem(res);
    throw new ApiError(res.status, problem, path);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const contentType = res.headers.get('content-type') ?? '';
  if (contentType.includes('json')) {
    return (await res.json()) as T;
  }

  return undefined as T;
}

export const api = {
  get: <T>(path: string, options?: Omit<RequestOptions, 'body'>) =>
    request<T>('GET', path, options),
  post: <T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>) =>
    request<T>('POST', path, { ...options, body }),
  put: <T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>) =>
    request<T>('PUT', path, { ...options, body }),
  patch: <T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>) =>
    request<T>('PATCH', path, { ...options, body }),
  delete: <T>(path: string, options?: Omit<RequestOptions, 'body'>) =>
    request<T>('DELETE', path, options),
};
