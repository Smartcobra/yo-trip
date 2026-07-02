import type { PnrInfo } from '../types';
import { formatSearchDate } from './date';

export function formatPnrDate(date: string): string {
  if (!date) return '—';
  try {
    return formatSearchDate(date);
  } catch {
    return date;
  }
}

export function pnrStatusClass(status: string): string {
  switch (status.toLowerCase()) {
    case 'issued':
    case 'confirmed':
      return 'status-confirmed';
    case 'pending':
    case 'pending_payment':
      return 'status-pending';
    case 'cancelled':
      return 'status-cancelled';
    default:
      return 'status-draft';
  }
}

export function capitalizeStatus(status: string): string {
  if (!status) return '—';
  return status.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
}

export function passengerName(p: { firstName: string; lastName: string }): string {
  return `${p.firstName} ${p.lastName}`.trim();
}

export function routeFromInfo(info: PnrInfo): string {
  const from = info.origin || info.originAirport;
  const to = info.destination || info.destinationAirport;
  if (from && to) return `${from} → ${to}`;
  return 'Flight details';
}
