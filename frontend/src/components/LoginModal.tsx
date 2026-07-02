import { useState } from 'react';
import { authApi } from '../api/client';
import { useAuth } from '../context/AppContext';

export default function LoginModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (!open) return null;

  const close = () => {
    setUsername('');
    setPassword('');
    setError('');
    onClose();
  };

  const handleLogin = async () => {
    setError('');
    setLoading(true);
    try {
      const res = await authApi.login({ username, password });
      login(res.data.token, res.data.user);
      close();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setError(msg || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="overlay" onClick={close}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>Welcome back</h2>
        <p style={{ margin: '0 0 20px', color: 'var(--text-muted)', fontSize: 14 }}>
          Sign in to your yo-trip account
        </p>
        <input
          className="form-input"
          placeholder="Username or email"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
        />
        <input
          className="form-input"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && !loading && username && password && handleLogin()}
          autoComplete="current-password"
        />
        <button
          type="button"
          className="btn-primary"
          style={{ width: '100%', marginTop: 4 }}
          disabled={loading || !username || !password}
          onClick={handleLogin}
        >
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}
