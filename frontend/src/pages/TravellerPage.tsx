import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { bookingApi, paymentApi } from '../api/client';
import { useAuth, useBooking } from '../context/AppContext';
import type { Flight, RazorpayOptions } from '../types';

export default function TravellerPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { selectedFlight, searchParams } = useBooking();
  const flight = selectedFlight as unknown as Flight | null;

  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [phone, setPhone] = useState(user?.mobile || '');
  const [coupon, setCoupon] = useState('');
  const [discount, setDiscount] = useState(0);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const pricing = useMemo(() => {
    if (!flight) return null;
    const base = Number(flight.price) || 3500;
    const surcharges = Math.round(base * 0.12);
    return { base, surcharges, total: Math.max(base + surcharges - discount, 0) };
  }, [flight, discount]);

  if (!flight || !pricing || !searchParams) { navigate('/search'); return null; }

  const applyCoupon = async () => {
    if (!coupon) return;
    try {
      const r = await bookingApi.validateCoupon(coupon, pricing.base + pricing.surcharges);
      const d = r.data as { valid: boolean; discountAmount: number };
      if (d.valid) setDiscount(Number(d.discountAmount));
      else setError('Invalid coupon');
    } catch { setError('Coupon validation failed'); }
  };

  const pay = async () => {
    setError('');
    if (!name || !email || !phone) { setError('Fill all fields'); return; }
    setLoading(true);
    try {
      const booking = (await bookingApi.create({
        flightData: flight,
        fromIata: searchParams.from,
        toIata: searchParams.to,
        baseFare: pricing.base,
        surcharges: pricing.surcharges,
        couponCode: coupon || undefined,
        travellerName: name,
        travellerEmail: email,
        travellerPhone: phone,
      })).data;

      const order = (await paymentApi.createOrder(booking.bookingId)).data;

      if (order.demoMode) {
        await paymentApi.verify({
          bookingId: booking.bookingId,
          razorpayOrderId: order.orderId,
          razorpayPaymentId: `pay_demo_${Date.now()}`,
          razorpaySignature: 'dev-signature',
        });
        navigate('/confirmation');
        return;
      }

      const options: RazorpayOptions = {
        key: order.keyId,
        currency: order.currency,
        amount: order.amount,
        name: 'yo-trip',
        description: 'Flight booking',
        image: '',
        order_id: order.orderId,
        prefill: { name, email, contact: phone },
        handler: async (res) => {
          await paymentApi.verify({
            bookingId: booking.bookingId,
            razorpayOrderId: res.razorpay_order_id,
            razorpayPaymentId: res.razorpay_payment_id,
            razorpaySignature: 'dev-signature',
          });
          navigate('/confirmation');
        },
      };
      new window.Razorpay(options).open();
    } catch {
      setError('Payment failed to start');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <Header />
      <div className="container page-grid">
        <div className="card">
          <h2>Traveller details</h2>
          <input className="form-input" placeholder="Full name" value={name}
            onChange={(e) => setName(e.target.value)} />
          <input className="form-input" placeholder="Email" value={email}
            onChange={(e) => setEmail(e.target.value)} />
          <input className="form-input" placeholder="Phone" value={phone}
            onChange={(e) => setPhone(e.target.value)} />
          <div style={{ display: 'flex', gap: 10 }}>
            <input className="form-input" style={{ marginBottom: 0, flex: 1 }}
              placeholder="Coupon code" value={coupon}
              onChange={(e) => setCoupon(e.target.value.toUpperCase())} />
            <button type="button" className="btn-outline" onClick={applyCoupon}>Apply</button>
          </div>
        </div>
        <div className="card">
          <h3>Pay ₹{pricing.total}</h3>
          {discount > 0 && (
            <p style={{ color: 'var(--success)', fontSize: 14 }}>Discount: -₹{discount}</p>
          )}
          {error && <p className="error">{error}</p>}
          <button type="button" className="btn-danger" style={{ width: '100%', marginTop: 12 }}
            disabled={loading} onClick={pay}>
            {loading ? 'Processing…' : 'Pay now'}
          </button>
        </div>
      </div>
    </div>
  );
}
