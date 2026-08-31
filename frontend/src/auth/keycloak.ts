let localToken: string | null = localStorage.getItem('hrms_token');

export function setLocalToken(token: string | null): void {
  localToken = token;
  if (token) {
    localStorage.setItem('hrms_token', token);
  } else {
    localStorage.removeItem('hrms_token');
  }
}

export async function getAccessToken(): Promise<string> {
  const token = localToken || localStorage.getItem('hrms_token');
  if (!token) {
    throw new Error('Access token is missing');
  }
  return token;
}
