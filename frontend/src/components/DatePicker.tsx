import { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import {
  addMonths,
  formatMonthYear,
  formatSearchDate,
  getCalendarDays,
  isBeforeDay,
  parseIso,
  sameDay,
  todayIso,
  toIso,
  WEEKDAYS,
} from '../utils/date';
import './DatePicker.css';

export default function DatePicker({
  value,
  onChange,
  min = todayIso(),
}: {
  value: string;
  onChange: (value: string) => void;
  min?: string;
}) {
  const rootRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const popoverRef = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const [position, setPosition] = useState({ top: 0, left: 0, width: 320 });
  const selected = parseIso(value);
  const minDate = parseIso(min);
  const today = parseIso(todayIso());
  const [viewMonth, setViewMonth] = useState(() => new Date(selected.getFullYear(), selected.getMonth(), 1));

  const updatePosition = () => {
    if (!triggerRef.current) return;
    const rect = triggerRef.current.getBoundingClientRect();
    const width = Math.min(320, window.innerWidth - 24);
    let left = rect.left;
    if (left + width > window.innerWidth - 12) {
      left = window.innerWidth - width - 12;
    }
    left = Math.max(12, left);
    setPosition({ top: rect.bottom + 10, left, width });
  };

  useEffect(() => {
    if (!open) return;
    updatePosition();
    const onPointerDown = (e: MouseEvent) => {
      const target = e.target as Node;
      if (
        !rootRef.current?.contains(target)
        && !popoverRef.current?.contains(target)
      ) {
        setOpen(false);
      }
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    const onReposition = () => updatePosition();
    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('keydown', onKeyDown);
    window.addEventListener('resize', onReposition);
    window.addEventListener('scroll', onReposition, true);
    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
      window.removeEventListener('resize', onReposition);
      window.removeEventListener('scroll', onReposition, true);
    };
  }, [open]);

  useEffect(() => {
    setViewMonth(new Date(selected.getFullYear(), selected.getMonth(), 1));
  }, [value]);

  const pickDate = (date: Date) => {
    if (isBeforeDay(date, minDate)) return;
    onChange(toIso(date));
    setOpen(false);
  };

  const canGoPrev = !isBeforeDay(
    new Date(viewMonth.getFullYear(), viewMonth.getMonth(), 1),
    new Date(minDate.getFullYear(), minDate.getMonth(), 1),
  );

  const calendar = open ? (
    <div
      ref={popoverRef}
      className="calendar-popover calendar-popover-portal glass"
      style={{ top: position.top, left: position.left, width: position.width }}
      role="dialog"
      aria-label="Choose departure date"
    >
      <div className="calendar-header">
        <button
          type="button"
          className="calendar-nav"
          disabled={!canGoPrev}
          onClick={() => setViewMonth((m) => addMonths(m, -1))}
          aria-label="Previous month"
        >
          ‹
        </button>
        <span className="calendar-month">{formatMonthYear(viewMonth)}</span>
        <button
          type="button"
          className="calendar-nav"
          onClick={() => setViewMonth((m) => addMonths(m, 1))}
          aria-label="Next month"
        >
          ›
        </button>
      </div>

      <div className="calendar-weekdays">
        {WEEKDAYS.map((day) => (
          <span key={day} className="calendar-weekday">{day}</span>
        ))}
      </div>

      <div className="calendar-grid">
        {getCalendarDays(viewMonth).map((date, i) => {
          if (!date) {
            return <span key={`empty-${i}`} className="calendar-day empty" />;
          }
          const disabled = isBeforeDay(date, minDate);
          const isSelected = sameDay(date, selected);
          const isToday = sameDay(date, today);
          return (
            <button
              key={toIso(date)}
              type="button"
              className={[
                'calendar-day',
                disabled ? 'disabled' : '',
                isSelected ? 'selected' : '',
                isToday ? 'today' : '',
              ].filter(Boolean).join(' ')}
              disabled={disabled}
              onClick={() => pickDate(date)}
            >
              {date.getDate()}
            </button>
          );
        })}
      </div>

      <div className="calendar-footer">
        <button
          type="button"
          className="calendar-today-btn"
          onClick={() => pickDate(today)}
        >
          Today
        </button>
      </div>
    </div>
  ) : null;

  return (
    <div className={`date-picker${open ? ' date-picker-open' : ''}`} ref={rootRef}>
      <span className="date-picker-label">Departure date</span>
      <button
        ref={triggerRef}
        type="button"
        className={`date-picker-trigger${open ? ' open' : ''}`}
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        aria-haspopup="dialog"
      >
        <span className="date-picker-icon" aria-hidden>📅</span>
        <span className="date-picker-value">{formatSearchDate(value)}</span>
        <span className="date-picker-chevron" aria-hidden>{open ? '▴' : '▾'}</span>
      </button>
      {calendar && createPortal(calendar, document.body)}
    </div>
  );
}
