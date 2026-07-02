import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { useBooking } from '../context/AppContext';
import type { Flight } from '../types';

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { selectedFlight } = useBooking();
  const flight = selectedFlight as unknown as Flight | null;

  const pricing = useMemo(() => {
    if (!flight) return null;
    const base = Number(flight.price) || 3500;
    const surcharges = Math.round(base * 0.12);
    return { base, surcharges, total: base + surcharges };
  }, [flight]);

  if (!flight || !pricing) { navigate('/search'); return null; }

  return (
    <div className="page">
      <Header />
      <div className="container page-grid">
        <div className="card">
          <h2>Review your flight</h2>
          <p><strong>{flight.airline.name}</strong> — {flight.flight.iata}</p>
          <p style={{ color: 'var(--text-muted)' }}>
            {flight.departure.iata} → {flight.arrival.iata}
          </p>
          <p style={{ fontSize: 14, color: 'var(--text-muted)', marginTop: 16 }}>
            Coupons: YOTRIP500 (₹500), YOTRIP300 (₹300)
          </p>
        </div>
        <div className="card">
          <h3>Fare summary</h3>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ color: 'var(--text-muted)' }}>Base fare</span>
            <span>₹{pricing.base}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ color: 'var(--text-muted)' }}>Taxes & fees</span>
            <span>₹{pricing.surcharges}</span>
          </div>
          <hr style={{ border: 'none', borderTop: '1px solid var(--glass-border)', margin: '16px 0' }} />
          <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 600, fontSize: '1.1rem' }}>
            <span>Total</span>
            <span style={{ color: 'var(--brand)' }}>₹{pricing.total}</span>
          </div>
          <button type="button" className="btn-primary" style={{ width: '100%', marginTop: 20 }}
            onClick={() => navigate('/final')}>Continue</button>
        </div>
      </div>
    </div>
  );
}
