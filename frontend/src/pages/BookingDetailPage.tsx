import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { bookingApi } from '../api/client';
import type { Booking } from '../types';
import {
  formatBookingDate,
  formatCurrency,
  statusClass,
  statusLabel,
} from '../utils/booking';
import './BookingDetailPage.css';

function FareRow({ label, value, highlight }: { label: string; value: string; highlight?: boolean }) {
  return (
    <div className={`fare-row${highlight ? ' fare-row-total' : ''}`}>
      <span>{label}</span>
      <span>{value}</span>
    </div>
  );
}

export default function BookingDetailPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const navigate = useNavigate();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!bookingId) {
      navigate('/bookings');
      return;
    }

    setLoading(true);
    bookingApi
      .get(bookingId)
      .then((r) => setBooking(r.data))
      .catch(() => setError('Booking not found or you do not have access.'))
      .finally(() => setLoading(false));
  }, [bookingId, navigate]);

  return (
    <div className="page">
      <Header />
      <div className="container booking-detail">
        <Link to="/bookings" className="back-link">← All bookings</Link>

        {loading && (
          <div className="card detail-skeleton" aria-hidden />
        )}

        {!loading && error && (
          <div className="card bookings-detail-empty">
            <span className="empty-icon" aria-hidden>!</span>
            <h2>Booking unavailable</h2>
            <p>{error}</p>
            <Link to="/bookings" className="btn-primary detail-cta">Back to bookings</Link>
          </div>
        )}

        {!loading && !error && booking && (
          <>
            <div className="detail-header">
              <div>
                <span className={`status-pill ${statusClass(booking.status)}`}>
                  {statusLabel(booking.status)}
                </span>
                <h1>Booking details</h1>
                <p className="detail-meta">
                  Booked on {formatBookingDate(booking.createdAt)}
                </p>
              </div>
              <div className="detail-id-block">
                <span className="detail-id-label">Booking ID</span>
                <code className="detail-id">{booking.bookingId}</code>
              </div>
            </div>

            <div className="detail-grid">
              <div className="card detail-route-card">
                <h2>Flight route</h2>
                <div className="detail-route">
                  <div className="detail-airport">
                    <span className="detail-iata">{booking.fromIata || '—'}</span>
                    <span className="detail-airport-label">Departure</span>
                  </div>
                  <div className="detail-route-visual" aria-hidden>
                    <div className="detail-route-arc" />
                    <span className="detail-route-plane">✈</span>
                  </div>
                  <div className="detail-airport detail-airport-right">
                    <span className="detail-iata">{booking.toIata || '—'}</span>
                    <span className="detail-airport-label">Arrival</span>
                  </div>
                </div>
              </div>

              <div className="card detail-fare-card">
                <h2>Fare breakdown</h2>
                <FareRow label="Base fare" value={formatCurrency(booking.baseFare)} />
                <FareRow label="Taxes & fees" value={formatCurrency(booking.surcharges)} />
                {booking.discount > 0 && (
                  <FareRow label="Discount" value={`−${formatCurrency(booking.discount)}`} />
                )}
                {booking.couponCode && (
                  <div className="coupon-tag">Coupon: {booking.couponCode}</div>
                )}
                <hr className="detail-divider" />
                <FareRow label="Total paid" value={formatCurrency(booking.totalAmount)} highlight />
              </div>

              {(booking.travellerName || booking.travellerEmail || booking.travellerPhone) && (
                <div className="card detail-traveller-card">
                  <h2>Traveller</h2>
                  {booking.travellerName && (
                    <div className="info-row">
                      <span className="info-label">Name</span>
                      <span>{booking.travellerName}</span>
                    </div>
                  )}
                  {booking.travellerEmail && (
                    <div className="info-row">
                      <span className="info-label">Email</span>
                      <span>{booking.travellerEmail}</span>
                    </div>
                  )}
                  {booking.travellerPhone && (
                    <div className="info-row">
                      <span className="info-label">Phone</span>
                      <span>{booking.travellerPhone}</span>
                    </div>
                  )}
                  {booking.gstNumber && (
                    <div className="info-row">
                      <span className="info-label">GST</span>
                      <span>{booking.gstNumber}</span>
                    </div>
                  )}
                </div>
              )}

              <div className="card detail-actions-card">
                <h2>Need help?</h2>
                <p className="detail-actions-text">
                  {booking.status === 'CONFIRMED'
                    ? 'Your booking is confirmed. Check your email for the e-ticket.'
                    : booking.status === 'PENDING_PAYMENT'
                      ? 'Complete payment to confirm this booking.'
                      : 'Contact support if you need to modify or cancel this booking.'}
                </p>
                <div className="detail-actions">
                  <Link to="/" className="btn-outline">Book another flight</Link>
                  <Link to="/bookings" className="btn-primary detail-cta-inline">My bookings</Link>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
