import axios from 'axios';
import type { AuthResponse, Booking, Flight, PaymentOrder, PnrInfo, PnrLookupMessage, User } from '../types';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
      localStorage.removeItem('token');
      if (window.location.pathname !== '/') {
        window.location.assign('/');
      }
    }
    return Promise.reject(error);
  },
);

export const authApi = {
  login: (payload: { username: string; password: string }) =>
    api.post<AuthResponse>('/auth/login', payload),
  me: () => api.get<User>('/auth/me'),
};

export const flightApi = {
  search: (from: string, to: string, sort = 'price_asc', date?: string) =>
    api.get<Flight[]>('/flights/search', { params: { from, to, sort, date } }),
  searchBot: (from: string, to: string, date?: string, bookingLeg?: string) =>
    api.get<{ flights: unknown[]; message: string; flightsFound: number }>('/flights/search', {
      params: { from, to, date, format: 'bot', bookingLeg },
    }),
};

export const bookingApi = {
  create: (payload: Record<string, unknown>) => api.post<Booking>('/bookings', payload),
  complete: (payload: Record<string, unknown>) =>
    api.post<{ pnrCode: string; transactionId: string; bookingId: string }>(
      '/bookings/complete',
      payload
    ),
  list: () => api.get<Booking[]>('/bookings'),
  get: (bookingId: string) => api.get<Booking>(`/bookings/${bookingId}`),
  validateCoupon: (code: string, amount: number) =>
    api.get('/coupons/validate', { params: { code, amount } }),
};

export const pnrApi = {
  lookup: (code: string) => api.get<PnrInfo>(`/pnr/${encodeURIComponent(code)}`),
  flightStatus: (code: string) =>
    api.get<PnrLookupMessage>(`/pnr/${encodeURIComponent(code)}/flight-status`),
  webCheckin: (code: string) =>
    api.get<PnrLookupMessage>(`/pnr/${encodeURIComponent(code)}/web-checkin`),
};

export const paymentApi = {
  createOrder: (bookingId: string) =>
    api.post<PaymentOrder>('/payments/create-order', { bookingId }),
  verify: (payload: {
    bookingId: string;
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }) => api.post('/payments/verify', payload),
};
