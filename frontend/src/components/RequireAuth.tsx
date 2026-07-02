import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AppContext';

export default function RequireAuth({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();

  if (loading) return null;
  if (!user) return <Navigate to="/" replace />;

  return <>{children}</>;
}
