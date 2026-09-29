import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { api, setSessionLostHandler, tokenStore, UserSummary } from "./api";

interface AuthState {
  user: UserSummary | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  reload: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    if (!tokenStore.access() && !tokenStore.refresh()) {
      setUser(null);
      return;
    }
    try {
      setUser(await api.me());
    } catch {
      tokenStore.clear();
      setUser(null);
    }
  }, []);

  useEffect(() => {
    setSessionLostHandler(() => setUser(null));
    reload().finally(() => setLoading(false));
  }, [reload]);

  const login = useCallback(async (email: string, password: string) => {
    tokenStore.save(await api.login(email, password));
    setUser(await api.me());
  }, []);

  const logout = useCallback(async () => {
    const refresh = tokenStore.refresh();
    tokenStore.clear();
    setUser(null);
    if (refresh) await api.logout(refresh).catch(() => undefined);
  }, []);

  const value = useMemo(() => ({ user, loading, login, logout, reload }), [user, loading, login, logout, reload]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
