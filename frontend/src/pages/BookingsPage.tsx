import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import { bookingApi } from '../api/client';
import type { Booking } from '../types';
import {
  formatBookingDate,
  formatCurrency,
  routeLabel,
  statusClass,
  statusLabel,
} from '../utils/booking';
import './BookingsPage.css';

export default function BookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    bookingApi
      .list()
      .then((r) => setBookings(r.data))
      .catch(() => setError('Could not load your bookings. Please try again.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="page">
      <Header />
      <div className="bookings-hero">
        <div className="container">
          <span className="bookings-badge">Your trips</span>
          <h1>My Bookings</h1>
          <p className="bookings-subtitle">View and manage all your flight reservations</p>
        </div>
      </div>

      <div className="container bookings-content">
        {loading && (
          <div className="bookings-grid">
            {[1, 2, 3].map((n) => (
              <div key={n} className="booking-card card skeleton-card" aria-hidden />
            ))}
          </div>
        )}

        {!loading && error && (
          <div className="card bookings-empty">
            <span className="empty-icon" aria-hidden>!</span>
            <h2>Something went wrong</h2>
            <p>{error}</p>
            <button type="button" className="btn-primary" onClick={() => window.location.reload()}>
              Retry
            </button>
          </div>
        )}

        {!loading && !error && bookings.length === 0 && (
          <div className="card bookings-empty">
            <span className="empty-icon" aria-hidden>✈</span>
            <h2>No bookings yet</h2>
            <p>Search for flights and book your next adventure.</p>
            <Link to="/" className="btn-primary bookings-cta">
              Search flights
            </Link>
          </div>
        )}

        {!loading && !error && bookings.length > 0 && (
          <div className="bookings-grid">
            {bookings.map((booking) => (
              <Link
                key={booking.bookingId}
                to={`/bookings/${booking.bookingId}`}
                className="booking-card card"
              >
                <div className="booking-card-top">
                  <span className={`status-pill ${statusClass(booking.status)}`}>
                    {statusLabel(booking.status)}
                  </span>
                  <span className="booking-date">{formatBookingDate(booking.createdAt)}</span>
                </div>

                <div className="booking-route">
                  <div className="route-endpoint">
                    <span className="route-code">{booking.fromIata || '—'}</span>
                    <span className="route-label">From</span>
                  </div>
                  <div className="route-connector" aria-hidden>
                    <span className="route-line" />
                    <span className="route-plane">✈</span>
                    <span className="route-line" />
                  </div>
                  <div className="route-endpoint route-endpoint-right">
                    <span className="route-code">{booking.toIata || '—'}</span>
                    <span className="route-label">To</span>
                  </div>
                </div>

                <div className="booking-card-bottom">
                  <div>
                    <span className="booking-id-label">Booking ID</span>
                    <span className="booking-id">{booking.bookingId}</span>
                  </div>
                  <div className="booking-amount">{formatCurrency(booking.totalAmount)}</div>
                </div>

                <span className="booking-card-hint">{routeLabel(booking)} · View details →</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
