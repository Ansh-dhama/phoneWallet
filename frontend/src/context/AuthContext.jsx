import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../services/walletApi';
import {
  clearStoredAuth,
  extractRole,
  getStoredAuth,
  persistAuth,
} from '../utils/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getStoredAuth());

  useEffect(() => {
    const sync = () => setAuth(getStoredAuth());
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);

  async function login(username, password) {
    const data = await authApi.login({ username, password });
    const next = {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      userId: data.userId,
      username,
      role: extractRole(data.accessToken),
    };
    persistAuth(next);
    setAuth(next);
    return next;
  }

  async function logout() {
    const refreshToken = auth?.refreshToken;
    try {
      if (refreshToken) await authApi.logout(refreshToken);
    } finally {
      clearStoredAuth();
      setAuth(null);
    }
  }

  const value = useMemo(
    () => ({
      auth,
      isAuthenticated: Boolean(auth?.accessToken),
      role: auth?.role || 'USER',
      login,
      logout,
    }),
    [auth],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
