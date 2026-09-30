let localToken: string | null = typeof window !== 'undefined' ? localStorage.getItem('hrms_token') : null;

export function setLocalToken(token: string | null): void {
  localToken = token;
  if (typeof window !== 'undefined') {
    if (token) {
      localStorage.setItem('hrms_token', token);
    } else {
      localStorage.removeItem('hrms_token');
    }
  }
}

export async function getAccessToken(): Promise<string> {
  const token = localToken || (typeof window !== 'undefined' ? localStorage.getItem('hrms_token') : null);
  if (!token) {
    throw new Error('Access token is missing');
  }
  return token;
}
