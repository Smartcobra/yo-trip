export interface Airport {
  city: string;
  airport: string;
  iata: string;
}

export interface FlightLocation {
  airport: string;
  iata: string;
  scheduled: string;
}

export interface Airline {
  name: string;
  iata: string;
}

export interface FlightInfo {
  iata: string;
  number?: string;
}

export interface Flight {
  flightDate: string;
  flightStatus: string;
  departure: FlightLocation;
  arrival: FlightLocation;
  airline: Airline;
  flight: FlightInfo;
  price: number;
}

export interface User {
  id: number;
  customerId: string;
  name: string;
  email: string;
  mobile: string;
}

export interface AuthResponse {
  token: string;
  user: User;
  isNewUser: boolean;
}

export interface Booking {
  bookingId: string;
  status: string;
  fromIata: string;
  toIata: string;
  baseFare: number;
  surcharges: number;
  discount: number;
  totalAmount: number;
  couponCode?: string | null;
  travellerName?: string | null;
  travellerEmail?: string | null;
  travellerPhone?: string | null;
  gstNumber?: string | null;
  flightData?: Record<string, unknown> | null;
  createdAt: string;
}

export interface PaymentOrder {
  orderId: string;
  currency: string;
  amount: number;
  keyId: string;
  bookingId: string;
  demoMode?: boolean;
}

export interface PnrPassenger {
  firstName: string;
  lastName: string;
  passengerType: string;
}

export interface PnrInfo {
  pnrCode: string;
  flightId: string;
  flightDate: string;
  origin: string;
  originAirport: string;
  destination: string;
  destinationAirport: string;
  departureTime: string;
  arrivalTime: string;
  duration: string;
  liveStatus: string;
  actualDeparture: string;
  actualArrival: string;
  pnrStatus: string;
  bookingStatus: string;
  passengers: PnrPassenger[];
}

export interface PnrLookupMessage {
  message: string;
  info: PnrInfo | null;
}

export interface SearchParams {
  from: string;
  to: string;
  date: string;
}

declare global {
  interface Window {
    Razorpay: new (options: RazorpayOptions) => { open: () => void };
  }
}

export interface RazorpayOptions {
  key: string;
  currency: string;
  amount: number;
  name: string;
  description: string;
  image: string;
  order_id: string;
  handler: (response: { razorpay_payment_id: string; razorpay_order_id: string }) => void;
  prefill?: { name?: string; email?: string; contact?: string };
}
