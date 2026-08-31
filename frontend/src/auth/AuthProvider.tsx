import { createContext, useMemo, useState, type ReactNode } from 'react';
import { setLocalToken } from './keycloak';

function decodeJwt(token: string) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

type AuthContextValue = {
  loading: boolean;
  error: string | null;
  authenticated: boolean;
  token?: string;
  login: (redirectPath?: string) => void;
  logout: () => void;
  authenticate: (token: string, redirectPath?: string) => void;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

type Props = {
  children: ReactNode;
};

export function AuthProvider({ children }: Props) {
  const [token, setTokenState] = useState<string | null>(() => {
    const saved = localStorage.getItem('hrms_token');
    if (saved) {
      const decoded = decodeJwt(saved);
      if (decoded && decoded.exp * 1000 > Date.now()) {
        setLocalToken(saved);
        return saved;
      }
      localStorage.removeItem('hrms_token');
      setLocalToken(null);
    }
    return null;
  });

  const [loading] = useState(false);
  const [error] = useState<string | null>(null);

  const authenticate = (newToken: string, redirectPath = '/dashboard') => {
    setTokenState(newToken);
    setLocalToken(newToken);
    window.location.href = redirectPath;
  };

  const logout = () => {
    setTokenState(null);
    setLocalToken(null);
    window.location.href = '/';
  };

  const login = (redirectPath = '/dashboard') => {
    window.location.href = `/login?redirect=${encodeURIComponent(redirectPath)}`;
  };

  const value = useMemo<AuthContextValue>(() => {
    return {
      loading,
      error,
      authenticated: !!token,
      token: token ?? undefined,
      login,
      logout,
      authenticate,
    };
  }, [token, loading, error]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
