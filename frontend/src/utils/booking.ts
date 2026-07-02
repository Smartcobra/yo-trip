import type { Booking } from '../types';

export function formatBookingDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString('en-IN', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return iso;
  }
}

export function formatCurrency(amount: number): string {
  return `₹${Number(amount).toLocaleString('en-IN')}`;
}

export function statusLabel(status: string): string {
  return status.replace(/_/g, ' ');
}

export function statusClass(status: string): string {
  switch (status) {
    case 'CONFIRMED':
      return 'status-confirmed';
    case 'PENDING_PAYMENT':
      return 'status-pending';
    case 'CANCELLED':
      return 'status-cancelled';
    default:
      return 'status-draft';
  }
}

export function routeLabel(booking: Booking): string {
  if (booking.fromIata && booking.toIata) {
    return `${booking.fromIata} → ${booking.toIata}`;
  }
  return 'Flight booking';
}
