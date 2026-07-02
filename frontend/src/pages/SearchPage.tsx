import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import FlightCard from '../components/FlightCard';
import AirportSelect from '../components/AirportSelect';
import DatePicker from '../components/DatePicker';
import { flightApi } from '../api/client';
import { useAuth, useBooking } from '../context/AppContext';
import type { Flight } from '../types';
import { formatSearchDate, todayIso } from '../utils/date';
import './SearchPage.css';

export default function SearchPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { searchParams, setSearchParams, setSelectedFlight } = useBooking();
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(true);
  const [sort, setSort] = useState('price_asc');
  const [from, setFrom] = useState(searchParams?.from || 'DEL');
  const [to, setTo] = useState(searchParams?.to || 'BOM');
  const [date, setDate] = useState(searchParams?.date || todayIso());

  useEffect(() => {
    if (!searchParams) { navigate('/'); return; }
    setFrom(searchParams.from);
    setTo(searchParams.to);
    setDate(searchParams.date || todayIso());
    setLoading(true);
    flightApi
      .search(searchParams.from, searchParams.to, sort, searchParams.date)
      .then((r) => setFlights(r.data))
      .catch(() => alert('Failed to load flights'))
      .finally(() => setLoading(false));
  }, [searchParams, sort, navigate]);

  const updateSearch = () => {
    if (!from || !to || !date) return alert('Select airports and date');
    if (from === to) return alert('From and To cannot be same');
    setSearchParams({ from, to, date });
  };

  const book = (flight: Flight) => {
    if (!user) return alert('Please login first');
    setSelectedFlight(flight as unknown as Record<string, unknown>);
    navigate('/checkout');
  };

  const displayDate = searchParams?.date ? formatSearchDate(searchParams.date) : '';

  return (
    <div className="page">
      <Header />
      <div className="search-hero">
        <div className="container">
          <span className="route-badge">{from} → {to}</span>
          <h2>Available flights</h2>
          {displayDate && <p className="search-date-label">{displayDate}</p>}
          <div className="toolbar card">
            <div className="toolbar-fields">
              <AirportSelect from={from} to={to}
                onChange={(f, v) => (f === 'from' ? setFrom(v) : setTo(v))} />
              <DatePicker value={date} onChange={setDate} />
            </div>
            <button type="button" className="btn-primary toolbar-btn"
              onClick={updateSearch}>Update</button>
          </div>
        </div>
      </div>
      <div className="container layout">
        <aside className="card filters">
          <h3>Sort by</h3>
          <label>
            <input type="radio" name="s" checked={sort === 'price_asc'}
              onChange={() => setSort('price_asc')} />
            Price: Low to High
          </label>
          <label>
            <input type="radio" name="s" checked={sort === 'price_desc'}
              onChange={() => setSort('price_desc')} />
            Price: High to Low
          </label>
        </aside>
        <main>
          {loading ? (
            <p className="loading-text">Finding the best fares…</p>
          ) : flights.length === 0 ? (
            <p className="card empty-state">No flights found for this route on the selected date.</p>
          ) : (
            flights.map((f, i) => (
              <FlightCard key={`${f.flight.iata}-${i}`} flight={f} onBook={book} />
            ))
          )}
        </main>
      </div>
    </div>
  );
}
