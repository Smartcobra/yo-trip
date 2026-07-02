import { Link } from 'react-router-dom';
import Header from '../components/Header';
import './ConfirmationPage.css';

export default function ConfirmationPage() {
  return (
    <div className="page">
      <Header />
      <div className="container">
        <div className="card confirmation-card">
          <div className="success-icon" aria-hidden>✓</div>
          <h1>Booking confirmed!</h1>
          <p>Your payment was successful. A confirmation email is on its way.</p>
          <Link to="/" className="btn-primary confirmation-btn">
            Back to home
          </Link>
        </div>
      </div>
    </div>
  );
}
