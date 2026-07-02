import { Routes, Route } from 'react-router-dom';
import RequireAuth from './components/RequireAuth';
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
      <Route path="/checkout" element={<RequireAuth><CheckoutPage /></RequireAuth>} />
      <Route path="/final" element={<RequireAuth><TravellerPage /></RequireAuth>} />
      <Route path="/confirmation" element={<RequireAuth><ConfirmationPage /></RequireAuth>} />
      <Route path="/bookings" element={<RequireAuth><BookingsPage /></RequireAuth>} />
      <Route path="/bookings/:bookingId" element={<RequireAuth><BookingDetailPage /></RequireAuth>} />
      <Route path="/pnr" element={<RequireAuth><PnrPage /></RequireAuth>} />
      <Route path="/pnr/:code" element={<RequireAuth><PnrPage /></RequireAuth>} />
    </Routes>
  );
}
