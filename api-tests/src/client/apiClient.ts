import request from "supertest";
import { config } from "../config/env";

export const api = request(config.apiBaseUrl);

let cachedToken: string = config.apiToken;

/**
 * Retrieves a valid bearer token.
 * If API_TOKEN was set via environment, it will be used.
 * Otherwise, it will authenticate once via POST /api/v1/auth/login and cache the token.
 */
export async function getAuthToken(): Promise<string> {
  if (cachedToken) {
    return cachedToken;
  }

  try {
    const res = await api
      .post("/api/v1/auth/login")
      .send({
        email: config.apiEmail,
        password: config.apiPassword,
      });

    if (res.status === 200 && res.body?.token) {
      cachedToken = res.body.token;
      return cachedToken;
    }
  } catch (err) {
    // If backend is unreachable or not yet ready, log warning
    console.warn("Could not authenticate automatically via /api/v1/auth/login:", (err as Error).message);
  }

  return "";
}

/**
 * Returns authorization header object if token is available.
 */
export async function authHeader(tokenOverride?: string): Promise<Record<string, string>> {
  const token = tokenOverride || (await getAuthToken());
  if (!token) {
    return {};
  }
  return {
    Authorization: `Bearer ${token}`,
  };
}

/**
 * Supertest GET wrapper with automated Bearer authorization
 */
export async function get(url: string, tokenOverride?: string) {
  const headers = await authHeader(tokenOverride);
  const req = api.get(url);
  for (const [key, value] of Object.entries(headers)) {
    req.set(key, value);
  }
  return req;
}

/**
 * Supertest POST wrapper with automated Bearer authorization
 */
export async function post(url: string, body?: unknown, tokenOverride?: string) {
  const headers = await authHeader(tokenOverride);
  const req = api.post(url);
  for (const [key, value] of Object.entries(headers)) {
    req.set(key, value);
  }
  if (body !== undefined) {
    req.send(body as object);
  }
  return req;
}

/**
 * Supertest PUT wrapper with automated Bearer authorization
 */
export async function put(url: string, body?: unknown, tokenOverride?: string) {
  const headers = await authHeader(tokenOverride);
  const req = api.put(url);
  for (const [key, value] of Object.entries(headers)) {
    req.set(key, value);
  }
  if (body !== undefined) {
    req.send(body as object);
  }
  return req;
}

/**
 * Supertest DELETE wrapper with automated Bearer authorization
 */
export async function del(url: string, tokenOverride?: string) {
  const headers = await authHeader(tokenOverride);
  const req = api.delete(url);
  for (const [key, value] of Object.entries(headers)) {
    req.set(key, value);
  }
  return req;
}
