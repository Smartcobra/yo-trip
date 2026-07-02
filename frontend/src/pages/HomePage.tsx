import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import AirportSelect from '../components/AirportSelect';
import DatePicker from '../components/DatePicker';
import { useBooking, useAuth } from '../context/AppContext';
import { todayIso } from '../utils/date';
import './HomePage.css';

export default function HomePage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setSearchParams } = useBooking();
  const [from, setFrom] = useState('DEL');
  const [to, setTo] = useState('BOM');
  const [date, setDate] = useState(todayIso());

  const search = () => {
    if (!from || !to) return alert('Select airports');
    if (!date) return alert('Select departure date');
    if (from === to) return alert('From and To cannot be same');
    setSearchParams({ from, to, date });
    navigate('/search');
  };

  return (
    <div className="page home">
      <Header />
      <section className="hero">
        <div className="container">
          <div className="search-card card">
            <h1>Where to next?</h1>
            <p className="subtitle">Search flights across India with yo-trip</p>
            <AirportSelect
              from={from}
              to={to}
              onChange={(f, v) => (f === 'from' ? setFrom(v) : setTo(v))}
            />
            <div className="search-date">
              <DatePicker value={date} onChange={setDate} />
            </div>
            <button type="button" className="btn-primary search-btn" onClick={search}>
              Search Flights
            </button>
            {user && (
              <p className="home-pnr-link">
                Already booked?{' '}
                <Link to="/pnr">Check PNR status</Link>
              </p>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}
