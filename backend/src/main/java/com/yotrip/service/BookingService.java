package com.yotrip.service;

import com.yotrip.dto.BookingResponse;
import com.yotrip.dto.CreateBookingRequest;
import com.yotrip.dto.CouponResponse;
import com.yotrip.entity.Booking;
import com.yotrip.entity.BookingStatus;
import com.yotrip.entity.FlightSchedule;
import com.yotrip.entity.Pnr;
import com.yotrip.entity.User;
import com.yotrip.exception.BadRequestException;
import com.yotrip.exception.NotFoundException;
import com.yotrip.repository.BookingRepository;
import com.yotrip.repository.FlightScheduleRepository;
import com.yotrip.repository.PnrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BookingService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final Map<String, CouponInfo> COUPONS = Stream.of(
            Map.entry("MMTSTANC", new CouponInfo("Flat ₹500 off on flights", BigDecimal.valueOf(500))),
            Map.entry("YOTRIP500", new CouponInfo("Flat ₹500 off on flights", BigDecimal.valueOf(500))),
            Map.entry("MMTZEST", new CouponInfo("Flat ₹300 off on flights", BigDecimal.valueOf(300))),
            Map.entry("YOTRIP300", new CouponInfo("Flat ₹300 off on flights", BigDecimal.valueOf(300)))
    ).collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

    private final BookingRepository bookingRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final PnrRepository pnrRepository;

    public BookingService(BookingRepository bookingRepository,
                          FlightScheduleRepository flightScheduleRepository,
                          PnrRepository pnrRepository) {
        this.bookingRepository = bookingRepository;
        this.flightScheduleRepository = flightScheduleRepository;
        this.pnrRepository = pnrRepository;
    }

    public CouponResponse validateCoupon(String code, BigDecimal amount) {
        CouponInfo info = COUPONS.get(code.toUpperCase());
        if (info == null) {
            return new CouponResponse(code, "Invalid coupon", BigDecimal.ZERO, false);
        }
        BigDecimal discount = info.discount().min(amount);
        return new CouponResponse(code.toUpperCase(), info.description(), discount, true);
    }

    @Transactional
    public BookingResponse createBooking(User user, CreateBookingRequest request) {
        BigDecimal discount = BigDecimal.ZERO;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            CouponResponse coupon = validateCoupon(request.couponCode(),
                    request.baseFare().add(request.surcharges()));
            if (!coupon.valid()) {
                throw new BadRequestException("Invalid coupon code");
            }
            discount = coupon.discountAmount();
        }

        BigDecimal total = request.baseFare().add(request.surcharges()).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        LocalDate flightDate = extractFlightDate(request.flightData());

        Pnr pnr = new Pnr();
        pnr.setPnrCode(generateUniquePnr());
        pnr.setCustomerId(user.getCustomerId());
        pnr.setPnrStatus("pending");
        pnr.setIssueDate(today);
        pnr.setValidUntil(flightDate.plusYears(1));
        pnr.setCreatedAt(now);
        pnr.setUpdatedAt(now);
        pnrRepository.save(pnr);

        Booking booking = new Booking();
        booking.setBookingId("BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        booking.setPnr(pnr);
        booking.setCustomerId(user.getCustomerId());
        booking.setFlightId(extractFlightId(request.flightData()));
        booking.setFlightDate(flightDate);
        booking.setTotalFare(request.baseFare());
        booking.setTaxCharges(request.surcharges());
        booking.setDiscount(discount);
        booking.setFinalAmount(total);
        booking.setBookingType("one-way");
        booking.setTotalPassengers(1);
        booking.setBookingStatus(BookingStatus.PENDING_PAYMENT.name());
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);

        return toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getUserBookings(User user) {
        if (user.getCustomerId() == null || user.getCustomerId().isBlank()) {
            return List.of();
        }
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(user.getCustomerId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public BookingResponse getBooking(User user, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!ownsBooking(user, booking)) {
            throw new NotFoundException("Booking not found");
        }
        return toResponse(booking);
    }

    public Booking getBookingEntity(User user, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!ownsBooking(user, booking)) {
            throw new NotFoundException("Booking not found");
        }
        return booking;
    }

    @Transactional
    public void confirmBooking(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(Instant.now());
        if (booking.getPnr() != null) {
            Pnr pnr = booking.getPnr();
            pnr.setPnrStatus("issued");
            pnr.setUpdatedAt(Instant.now());
            pnrRepository.save(pnr);
        }
        bookingRepository.save(booking);
    }

    private boolean ownsBooking(User user, Booking booking) {
        return user.getCustomerId() != null
                && user.getCustomerId().equals(booking.getCustomerId());
    }

    private BookingResponse toResponse(Booking booking) {
        String fromIata = null;
        String toIata = null;
        if (booking.getFlightId() != null) {
            FlightSchedule schedule = flightScheduleRepository.findById(booking.getFlightId()).orElse(null);
            if (schedule != null) {
                fromIata = schedule.getOriginAirportCode();
                toIata = schedule.getDestinationAirportCode();
            }
        }

        return new BookingResponse(
                booking.getBookingId(),
                booking.getStatus().name(),
                fromIata,
                toIata,
                booking.getTotalFare(),
                booking.getTaxCharges(),
                booking.getDiscount(),
                booking.getFinalAmount(),
                null,
                null,
                null,
                null,
                null,
                null,
                booking.getCreatedAt()
        );
    }

    private String extractFlightId(Map<String, Object> flightData) {
        if (flightData == null) {
            return null;
        }
        Object flightId = flightData.get("flightId");
        if (flightId == null) {
            flightId = flightData.get("flightNumber");
        }
        if (flightId == null) {
            Object flight = flightData.get("flight");
            if (flight instanceof Map<?, ?> flightMap) {
                Object iata = flightMap.get("iata");
                if (iata != null) {
                    flightId = iata;
                } else {
                    Object number = flightMap.get("number");
                    if (number != null) {
                        flightId = "6E" + number;
                    }
                }
            }
        }
        return flightId != null ? flightId.toString() : null;
    }

    private LocalDate extractFlightDate(Map<String, Object> flightData) {
        if (flightData == null) {
            return LocalDate.now();
        }
        Object date = flightData.get("flightDate");
        if (date == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(date.toString());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String generateUniquePnr() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(PNR_CHARS.charAt(RANDOM.nextInt(PNR_CHARS.length())));
            }
            String code = sb.toString();
            if (!pnrRepository.existsByPnrCode(code)) {
                return code;
            }
        }
        throw new BadRequestException("Unable to generate unique PNR");
    }

    private record CouponInfo(String description, BigDecimal discount) {}
}
