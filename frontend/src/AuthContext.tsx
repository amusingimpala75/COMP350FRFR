import { createContext, useContext, useEffect, useMemo, useState } from 'react';

export interface AuthUser {
  id: number;
  username: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  signup: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

async function parseError(res: Response): Promise<string> {
  try {
    const json = (await res.json()) as { error?: string };
    return json.error ?? 'Request failed';
  } catch {
    return 'Request failed';
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshMe = async () => {
    try {
      const res = await fetch('/auth/me', { credentials: 'include' });
      if (!res.ok) {
        setUser(null);
        return;
      }
      const me = (await res.json()) as AuthUser;
      setUser(me);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshMe();
  }, []);

  const login = async (username: string, password: string) => {
    const res = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ username, password }),
    });

    if (!res.ok) {
      throw new Error(await parseError(res));
    }

    const loggedIn = (await res.json()) as AuthUser;
    setUser(loggedIn);
  };

  const signup = async (username: string, password: string) => {
    const res = await fetch('/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ username, password }),
    });

    if (!res.ok) {
      throw new Error(await parseError(res));
    }

    const signedUp = (await res.json()) as AuthUser;
    setUser(signedUp);
  };

  const logout = async () => {
    await fetch('/auth/logout', {
      method: 'POST',
      credentials: 'include',
    });
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, loading, login, signup, logout, refreshMe }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}

