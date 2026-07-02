import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/client';
import type { SearchParams, User } from '../types';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
}

interface BookingContextValue {
  searchParams: SearchParams | null;
  setSearchParams: (p: SearchParams) => void;
  selectedFlight: Record<string, unknown> | null;
  setSelectedFlight: (f: Record<string, unknown> | null) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);
const BookingContext = createContext<BookingContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return;
    }
    authApi
      .me()
      .then((r) => setUser(r.data))
      .catch(() => localStorage.removeItem('token'))
      .finally(() => setLoading(false));
  }, []);

  const value = useMemo(
    () => ({
      user,
      loading,
      login: (token: string, u: User) => {
        localStorage.setItem('token', token);
        setUser(u);
      },
      logout: () => {
        localStorage.removeItem('token');
        setUser(null);
      },
    }),
    [user, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function BookingProvider({ children }: { children: ReactNode }) {
  const [searchParams, setSearchParamsState] = useState<SearchParams | null>(() => {
    const s = localStorage.getItem('searchParams');
    return s ? JSON.parse(s) : null;
  });
  const [selectedFlight, setSelectedFlightState] = useState<Record<string, unknown> | null>(() => {
    const s = localStorage.getItem('selectedFlight');
    return s ? JSON.parse(s) : null;
  });

  const setSearchParams = (p: SearchParams) => {
    localStorage.setItem('searchParams', JSON.stringify(p));
    setSearchParamsState(p);
  };

  const setSelectedFlight = (f: Record<string, unknown> | null) => {
    if (f) localStorage.setItem('selectedFlight', JSON.stringify(f));
    else localStorage.removeItem('selectedFlight');
    setSelectedFlightState(f);
  };

  const value = useMemo(
    () => ({ searchParams, setSearchParams, selectedFlight, setSelectedFlight }),
    [searchParams, selectedFlight],
  );

  return <BookingContext.Provider value={value}>{children}</BookingContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth outside provider');
  return ctx;
}

export function useBooking() {
  const ctx = useContext(BookingContext);
  if (!ctx) throw new Error('useBooking outside provider');
  return ctx;
}
