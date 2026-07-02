import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import SearchPage from './pages/SearchPage';
import CheckoutPage from './pages/CheckoutPage';
import TravellerPage from './pages/TravellerPage';
import ConfirmationPage from './pages/ConfirmationPage';
import BookingsPage from './pages/BookingsPage';
import BookingDetailPage from './pages/BookingDetailPage';
import PnrPage from './pages/PnrPage';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/search" element={<SearchPage />} />
      <Route path="/checkout" element={<CheckoutPage />} />
      <Route path="/final" element={<TravellerPage />} />
      <Route path="/confirmation" element={<ConfirmationPage />} />
      <Route path="/bookings" element={<BookingsPage />} />
      <Route path="/bookings/:bookingId" element={<BookingDetailPage />} />
      <Route path="/pnr" element={<PnrPage />} />
      <Route path="/pnr/:code" element={<PnrPage />} />
    </Routes>
  );
}
