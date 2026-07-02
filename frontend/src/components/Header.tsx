import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AppContext';
import LoginModal from './LoginModal';
import './Header.css';

export default function Header() {
  const { user, logout } = useAuth();
  const [loginOpen, setLoginOpen] = useState(false);

  return (
    <>
      <header className="header glass">
        <div className="header-inner container">
          <Link to="/" className="brand">
            <span className="brand-icon" aria-hidden>✈</span>
            <span className="brand-text">yo-trip</span>
          </Link>
          <div className="header-right">
            {user ? (
              <>
                <Link to="/pnr" className="nav-link">PNR Status</Link>
                <Link to="/bookings" className="nav-link">My Bookings</Link>
                <span className="greeting">Hi, {user.name}</span>
                <button type="button" className="btn-outline" onClick={logout}>Logout</button>
              </>
            ) : (
              <button type="button" className="btn-outline" onClick={() => setLoginOpen(true)}>
                Login
              </button>
            )}
          </div>
        </div>
      </header>
      <LoginModal open={loginOpen} onClose={() => setLoginOpen(false)} />
    </>
  );
}
