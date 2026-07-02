import type { Flight } from '../types';
import './FlightCard.css';

function formatTime(iso: string) {
  try {
    return new Date(iso).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  } catch {
    return iso;
  }
}

export default function FlightCard({ flight, onBook }: { flight: Flight; onBook: (f: Flight) => void }) {
  return (
    <div className="flight-card card">
      <div className="flight-row">
        <div>
          <strong>{flight.airline.name}</strong>
          <p className="muted">{flight.flight.iata} · {flight.flightStatus}</p>
        </div>
        <div className="times">
          <div><b>{formatTime(flight.departure.scheduled)}</b><span>{flight.departure.iata}</span></div>
          <span className="arrow">→</span>
          <div><b>{formatTime(flight.arrival.scheduled)}</b><span>{flight.arrival.iata}</span></div>
        </div>
        <div className="price-col">
          <h3>₹{flight.price}</h3>
          <button type="button" className="btn-primary" onClick={() => onBook(flight)}>BOOK NOW</button>
        </div>
      </div>
    </div>
  );
}
