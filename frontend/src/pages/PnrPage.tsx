import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Header from '../components/Header';
import { pnrApi } from '../api/client';
import type { PnrInfo, PnrLookupMessage } from '../types';
import {
  capitalizeStatus,
  formatPnrDate,
  passengerName,
  pnrStatusClass,
  routeFromInfo,
} from '../utils/pnr';
import './PnrPage.css';

type PnrTab = 'overview' | 'flight-status' | 'web-checkin';

function PnrRouteCard({ info }: { info: PnrInfo }) {
  return (
    <div className="pnr-route-card">
      <div className="pnr-route-header">
        <div>
          <span className="pnr-flight-id">{info.flightId || '—'}</span>
          <span className="pnr-flight-date">{formatPnrDate(info.flightDate)}</span>
        </div>
        <span className={`status-pill ${pnrStatusClass(info.liveStatus || 'scheduled')}`}>
          {info.liveStatus || 'Scheduled'}
        </span>
      </div>

      <div className="pnr-route-visual">
        <div className="pnr-airport">
          <span className="pnr-city">{info.origin || '—'}</span>
          <span className="pnr-airport-name">{info.originAirport}</span>
          <span className="pnr-time">{info.departureTime}</span>
        </div>
        <div className="pnr-connector" aria-hidden>
          <span className="pnr-duration">{info.duration}</span>
          <div className="pnr-connector-line">
            <span className="pnr-plane">✈</span>
          </div>
        </div>
        <div className="pnr-airport pnr-airport-right">
          <span className="pnr-city">{info.destination || '—'}</span>
          <span className="pnr-airport-name">{info.destinationAirport}</span>
          <span className="pnr-time">{info.arrivalTime}</span>
        </div>
      </div>
    </div>
  );
}

function PnrMetaGrid({ info }: { info: PnrInfo }) {
  return (
    <div className="pnr-meta-grid">
      <div className="pnr-meta-item">
        <span className="pnr-meta-label">PNR status</span>
        <span className={`status-pill ${pnrStatusClass(info.pnrStatus)}`}>
          {capitalizeStatus(info.pnrStatus)}
        </span>
      </div>
      <div className="pnr-meta-item">
        <span className="pnr-meta-label">Booking status</span>
        <span className={`status-pill ${pnrStatusClass(info.bookingStatus)}`}>
          {capitalizeStatus(info.bookingStatus)}
        </span>
      </div>
      {info.actualDeparture && (
        <div className="pnr-meta-item">
          <span className="pnr-meta-label">Actual departure</span>
          <span>{info.actualDeparture}</span>
        </div>
      )}
      {info.actualArrival && (
        <div className="pnr-meta-item">
          <span className="pnr-meta-label">Actual arrival</span>
          <span>{info.actualArrival}</span>
        </div>
      )}
    </div>
  );
}

function PassengerList({ passengers }: { passengers: PnrInfo['passengers'] }) {
  if (!passengers.length) {
    return <p className="pnr-empty-passengers">No passenger details on file.</p>;
  }

  return (
    <ul className="pnr-passenger-list">
      {passengers.map((p) => (
        <li key={`${p.firstName}-${p.lastName}-${p.passengerType}`} className="pnr-passenger">
          <span className="pnr-passenger-avatar" aria-hidden>
            {p.firstName.charAt(0).toUpperCase()}
          </span>
          <div>
            <span className="pnr-passenger-name">{passengerName(p)}</span>
            <span className="pnr-passenger-type">{p.passengerType}</span>
          </div>
        </li>
      ))}
    </ul>
  );
}

function MessagePanel({
  title,
  message,
  info,
  variant,
}: {
  title: string;
  message: string;
  info: PnrInfo | null;
  variant: 'status' | 'checkin';
}) {
  const isError = !info;
  const isCheckinBlocked =
    variant === 'checkin' && info && info.bookingStatus.toLowerCase() !== 'confirmed';

  return (
    <div className="card pnr-message-panel">
      <div className="pnr-message-header">
        <h2>{title}</h2>
        {isError && <span className="status-pill status-cancelled">Not found</span>}
        {isCheckinBlocked && <span className="status-pill status-pending">Unavailable</span>}
        {!isError && !isCheckinBlocked && variant === 'checkin' && (
          <span className="status-pill status-confirmed">Eligible</span>
        )}
      </div>

      <p className={`pnr-message-text${isError || isCheckinBlocked ? ' pnr-message-warn' : ''}`}>
        {message.split("Type 'exit'")[0].trim()}
      </p>

      {info && <PnrRouteCard info={info} />}
      {info && <PnrMetaGrid info={info} />}
      {info && info.passengers.length > 0 && (
        <div className="pnr-passengers-section">
          <h3>Passengers</h3>
          <PassengerList passengers={info.passengers} />
        </div>
      )}

      {variant === 'checkin' && info && info.bookingStatus.toLowerCase() === 'confirmed' && (
        <div className="pnr-checkin-note">
          <span aria-hidden>🛫</span>
          <p>
            Web check-in opens 48 hours before departure and closes 60 minutes before departure.
            Visit <strong>goindigo.in</strong> to complete your check-in.
          </p>
        </div>
      )}
    </div>
  );
}

export default function PnrPage() {
  const { code: routeCode } = useParams<{ code?: string }>();
  const navigate = useNavigate();
  const [pnrInput, setPnrInput] = useState(routeCode?.toUpperCase() ?? '');
  const [activeCode, setActiveCode] = useState('');
  const [tab, setTab] = useState<PnrTab>('overview');
  const [overview, setOverview] = useState<PnrInfo | null>(null);
  const [flightStatus, setFlightStatus] = useState<PnrLookupMessage | null>(null);
  const [webCheckin, setWebCheckin] = useState<PnrLookupMessage | null>(null);
  const [loading, setLoading] = useState(false);
  const [tabLoading, setTabLoading] = useState(false);
  const [error, setError] = useState('');

  const lookupPnr = useCallback(async (code: string) => {
    const normalized = code.trim().toUpperCase();
    if (!normalized) {
      setError('Please enter your PNR code.');
      return;
    }

    setLoading(true);
    setError('');
    setOverview(null);
    setFlightStatus(null);
    setWebCheckin(null);
    setActiveCode(normalized);
    setTab('overview');

    try {
      const res = await pnrApi.lookup(normalized);
      setOverview(res.data);
      navigate(`/pnr/${normalized}`, { replace: true });
    } catch {
      setError(`We could not find any booking with PNR ${normalized}. Please check the code and try again.`);
      setActiveCode('');
      navigate('/pnr', { replace: true });
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => {
    if (routeCode && routeCode.toUpperCase() !== activeCode) {
      setPnrInput(routeCode.toUpperCase());
      lookupPnr(routeCode);
    }
  }, [routeCode, activeCode, lookupPnr]);

  useEffect(() => {
    if (!activeCode || tab === 'overview') return;

    const loadTab = async () => {
      setTabLoading(true);
      try {
        if (tab === 'flight-status') {
          const res = await pnrApi.flightStatus(activeCode);
          setFlightStatus(res.data);
        } else {
          const res = await pnrApi.webCheckin(activeCode);
          setWebCheckin(res.data);
        }
      } catch {
        setError('Something went wrong. Please try again.');
      } finally {
        setTabLoading(false);
      }
    };

    if (tab === 'flight-status' && !flightStatus) loadTab();
    if (tab === 'web-checkin' && !webCheckin) loadTab();
  }, [tab, activeCode, flightStatus, webCheckin]);

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    lookupPnr(pnrInput);
  };

  return (
    <div className="page">
      <Header />
      <div className="pnr-hero">
        <div className="container">
          <span className="pnr-badge">Manage trip</span>
          <h1>PNR Lookup</h1>
          <p className="pnr-subtitle">Check booking details, flight status, and web check-in</p>

          <form className="card pnr-search" onSubmit={onSubmit}>
            <label htmlFor="pnr-code" className="pnr-search-label">Enter your 6-character PNR</label>
            <div className="pnr-search-row">
              <input
                id="pnr-code"
                className="form-input pnr-input"
                value={pnrInput}
                onChange={(e) => setPnrInput(e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6))}
                placeholder="e.g. AB12CD"
                maxLength={6}
                autoComplete="off"
                spellCheck={false}
              />
              <button type="submit" className="btn-primary pnr-search-btn" disabled={loading}>
                {loading ? 'Searching…' : 'Lookup PNR'}
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="container pnr-content">
        {error && (
          <div className="card pnr-error">
            <span className="pnr-error-icon" aria-hidden>!</span>
            <p>{error}</p>
          </div>
        )}

        {loading && (
          <div className="card pnr-skeleton" aria-hidden />
        )}

        {!loading && overview && (
          <>
            <div className="pnr-result-header">
              <div>
                <span className="pnr-code-label">PNR</span>
                <h2 className="pnr-code">{overview.pnrCode}</h2>
                <p className="pnr-route-label">{routeFromInfo(overview)}</p>
              </div>
            </div>

            <div className="pnr-tabs" role="tablist" aria-label="PNR actions">
              <button
                type="button"
                role="tab"
                aria-selected={tab === 'overview'}
                className={`pnr-tab${tab === 'overview' ? ' active' : ''}`}
                onClick={() => setTab('overview')}
              >
                Overview
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={tab === 'flight-status'}
                className={`pnr-tab${tab === 'flight-status' ? ' active' : ''}`}
                onClick={() => setTab('flight-status')}
              >
                Flight Status
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={tab === 'web-checkin'}
                className={`pnr-tab${tab === 'web-checkin' ? ' active' : ''}`}
                onClick={() => setTab('web-checkin')}
              >
                Web Check-in
              </button>
            </div>

            {tab === 'overview' && (
              <div className="card pnr-overview">
                <PnrRouteCard info={overview} />
                <PnrMetaGrid info={overview} />
                <div className="pnr-passengers-section">
                  <h3>Passengers</h3>
                  <PassengerList passengers={overview.passengers} />
                </div>
              </div>
            )}

            {tab === 'flight-status' && (
              tabLoading ? (
                <div className="card pnr-skeleton pnr-skeleton-short" aria-hidden />
              ) : flightStatus ? (
                <MessagePanel
                  title="Flight status"
                  message={flightStatus.message}
                  info={flightStatus.info}
                  variant="status"
                />
              ) : null
            )}

            {tab === 'web-checkin' && (
              tabLoading ? (
                <div className="card pnr-skeleton pnr-skeleton-short" aria-hidden />
              ) : webCheckin ? (
                <MessagePanel
                  title="Web check-in"
                  message={webCheckin.message}
                  info={webCheckin.info}
                  variant="checkin"
                />
              ) : null
            )}
          </>
        )}

        {!loading && !overview && !error && (
          <div className="card pnr-hint">
            <span className="pnr-hint-icon" aria-hidden>🎫</span>
            <h2>Find your booking</h2>
            <p>Enter the 6-character PNR from your confirmation email to view trip details, live status, and check-in eligibility.</p>
          </div>
        )}
      </div>
    </div>
  );
}
