import { useEffect, useState } from 'react';
import type { Airport } from '../types';
import './AirportSelect.css';

const URL = 'https://raw.githubusercontent.com/ashhadulislam/JSON-Airports-India/master/airports.json';

export default function AirportSelect({
  from, to, onChange,
}: {
  from: string;
  to: string;
  onChange: (field: 'from' | 'to', value: string) => void;
}) {
  const [airports, setAirports] = useState<Airport[]>([]);

  useEffect(() => {
    fetch(URL)
      .then((r) => r.json())
      .then((data) => {
        const list = Array.isArray(data) ? data : data?.airports;
        if (!Array.isArray(list)) {
          setAirports([]);
          return;
        }
        setAirports(
          list.map((a: Record<string, string>) => ({
            iata: a.IATA_code ?? a.iata ?? '',
            city: a.city_name ?? a.city ?? '',
            airport: a.airport_name ?? a.airport ?? '',
          })).filter((a) => a.iata),
        );
      })
      .catch(() => setAirports([]));
  }, []);

  return (
    <div className="airport-select">
      <label>FROM
        <select value={from} onChange={(e) => onChange('from', e.target.value)}>
          <option value="">Select</option>
          {airports.map((a) => (
            <option key={`f-${a.iata}`} value={a.iata}>{a.city} ({a.iata})</option>
          ))}
        </select>
      </label>
      <label>TO
        <select value={to} onChange={(e) => onChange('to', e.target.value)}>
          <option value="">Select</option>
          {airports.map((a) => (
            <option key={`t-${a.iata}`} value={a.iata}>{a.city} ({a.iata})</option>
          ))}
        </select>
      </label>
    </div>
  );
}
