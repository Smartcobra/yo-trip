package com.yotrip.service;

import com.yotrip.dto.*;
import com.yotrip.entity.*;
import com.yotrip.exception.BadRequestException;
import com.yotrip.exception.NotFoundException;
import com.yotrip.repository.*;
import com.yotrip.util.CityIataResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PnrService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PNR_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private static final Map<String, String> TITLE_TO_GENDER = Map.of(
            "Mr", "Male",
            "Mrs", "Female",
            "Miss", "Female",
            "Ms", "Female",
            "Master", "Male",
            "Mx", "Other",
            "Other", "Other"
    );
    private static final Map<String, String> CATEGORY_TO_TYPE = Map.of(
            "adult", "Adult", "child", "Child", "infant", "Infant"
    );

    private final PnrRepository pnrRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final FlightScheduleRepository flightScheduleRepository;

    public PnrService(PnrRepository pnrRepository,
                      BookingRepository bookingRepository,
                      PassengerRepository passengerRepository,
                      PaymentRecordRepository paymentRecordRepository,
                      FlightScheduleRepository flightScheduleRepository) {
        this.pnrRepository = pnrRepository;
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.flightScheduleRepository = flightScheduleRepository;
    }

    @Transactional
    public CompleteBookingResponse completeBooking(CompleteBookingRequest request, User user) {
        int adults = request.adults() != null ? request.adults() : 1;
        int children = request.children() != null ? request.children() : 0;
        int totalPassengers = adults + children;
        String tripType = request.tripType() != null ? request.tripType() : "one-way";

        SelectedFlightDto outbound = request.selectedOutboundFlight() != null
                ? request.selectedOutboundFlight()
                : request.selectedFlight();
        if (outbound == null || outbound.flightNumber() == null || outbound.flightNumber().isBlank()) {
            throw new BadRequestException("Outbound flight selection is required");
        }

        SelectedFlightDto returnFlight = request.selectedReturnFlight();
        boolean isRoundTrip = returnFlight != null
                && returnFlight.flightNumber() != null
                && !returnFlight.flightNumber().isBlank();

        BigDecimal outboundPrice = outbound.price() != null ? outbound.price() : BigDecimal.ZERO;
        BigDecimal returnPrice = isRoundTrip && returnFlight.price() != null
                ? returnFlight.price() : BigDecimal.ZERO;
        BigDecimal totalAmount = outboundPrice.add(returnPrice).multiply(BigDecimal.valueOf(adults));

        LocalDate travelDate = parseDate(request.travelDate());
        LocalDate returnDate = isRoundTrip ? parseDate(request.returnDate()) : null;

        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        LocalDate validUntil = travelDate.plusYears(1);

        Pnr pnr = new Pnr();
        pnr.setPnrCode(generateUniquePnr());
        pnr.setCustomerId(user.getCustomerId());
        pnr.setPnrStatus("issued");
        pnr.setIssueDate(today);
        pnr.setValidUntil(validUntil);
        pnr.setCreatedAt(now);
        pnr.setUpdatedAt(now);
        pnrRepository.save(pnr);

        String outboundBookingRef = "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        Booking outboundBooking = buildBooking(
                outboundBookingRef, pnr, user, outbound.flightNumber(), travelDate,
                totalPassengers, tripType, outboundPrice.multiply(BigDecimal.valueOf(adults)),
                request.departureCity(), request.destinationCity(), now
        );
        bookingRepository.save(outboundBooking);

        if (isRoundTrip) {
            String returnBookingRef = "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            Booking returnBooking = buildBooking(
                    returnBookingRef, pnr, user, returnFlight.flightNumber(), returnDate,
                    totalPassengers, tripType, returnPrice.multiply(BigDecimal.valueOf(adults)),
                    request.destinationCity(), request.departureCity(), now
            );
            bookingRepository.save(returnBooking);
        }

        List<PassengerRequestDto> passengers = request.passengers() != null ? request.passengers() : List.of();
        for (PassengerRequestDto p : passengers) {
            Passenger passenger = new Passenger();
            passenger.setPassengerId("PAX" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
            passenger.setBookingId(outboundBooking.getBookingId());
            passenger.setFirstName(p.firstName());
            passenger.setLastName(p.lastName());
            passenger.setGender(resolveGender(p));
            passenger.setPassengerType(CATEGORY_TO_TYPE.getOrDefault(
                    p.ageCategory() != null ? p.ageCategory().toLowerCase() : "adult", "Adult"));
            passenger.setCreatedAt(now);
            passengerRepository.save(passenger);
        }

        String transactionId = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentId("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        payment.setBookingId(outboundBooking.getBookingId());
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
        payment.setTransactionId(transactionId);
        payment.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "WhatsApp Pay");
        payment.setPaymentGateway("IndiGo Pay");
        payment.setCreatedAt(now);
        paymentRecordRepository.save(payment);

        return new CompleteBookingResponse(pnr.getPnrCode(), transactionId, outboundBookingRef);
    }

    public PnrInfoResponse fetchPnrInfo(String pnrCode) {
        Pnr pnr = pnrRepository.findByPnrCode(pnrCode.toUpperCase())
                .orElse(null);
        if (pnr == null) {
            return null;
        }

        Booking booking = bookingRepository.findFirstByPnrOrderByCreatedAtAsc(pnr)
                .orElseThrow(() -> new NotFoundException("Booking not found for PNR"));

        FlightSchedule schedule = flightScheduleRepository.findById(
                booking.getFlightId() != null ? booking.getFlightId() : ""
        ).orElse(null);

        CityIataResolver.AirportInfo origin = schedule != null
                ? CityIataResolver.airportInfo(schedule.getOriginAirportCode())
                : CityIataResolver.airportInfo("");
        CityIataResolver.AirportInfo destination = schedule != null
                ? CityIataResolver.airportInfo(schedule.getDestinationAirportCode())
                : CityIataResolver.airportInfo("");

        String departureTime = schedule != null ? formatTime(schedule.getDepartureTime()) : "—";
        String arrivalTime = schedule != null ? formatTime(schedule.getArrivalTime()) : "—";
        String duration = schedule != null ? formatDuration(schedule.getFlightDurationMinutes()) : "—";

        List<PnrPassengerDto> passengers = passengerRepository.findByBookingId(booking.getBookingId()).stream()
                .map(p -> new PnrPassengerDto(p.getFirstName(), p.getLastName(), p.getPassengerType()))
                .toList();

        return new PnrInfoResponse(
                pnr.getPnrCode(),
                booking.getFlightId(),
                booking.getFlightDate() != null ? booking.getFlightDate().toString() : "",
                origin.city(),
                origin.airportName(),
                destination.city(),
                destination.airportName(),
                departureTime,
                arrivalTime,
                duration,
                "Scheduled",
                "",
                "",
                pnr.getPnrStatus(),
                booking.getBookingStatus() != null ? booking.getBookingStatus().toLowerCase() : "draft",
                passengers
        );
    }

    public PnrLookupMessageResponse flightStatus(String pnrCode) {
        PnrInfoResponse info = fetchPnrInfo(pnrCode);
        if (info == null) {
            return new PnrLookupMessageResponse(
                    "We could not find any booking with PNR " + pnrCode.toUpperCase()
                            + ". Please check the PNR and try again.",
                    null
            );
        }
        return new PnrLookupMessageResponse(formatFlightStatus(info), info);
    }

    public PnrLookupMessageResponse webCheckin(String pnrCode) {
        PnrInfoResponse info = fetchPnrInfo(pnrCode);
        if (info == null) {
            return new PnrLookupMessageResponse(
                    "We could not find any booking with PNR " + pnrCode.toUpperCase()
                            + ". Please check the PNR and try again.",
                    null
            );
        }
        return new PnrLookupMessageResponse(formatWebCheckin(info), info);
    }

    private Booking buildBooking(String externalId, Pnr pnr, User user, String flightId,
                                 LocalDate flightDate, int totalPassengers, String tripType,
                                 BigDecimal amount, String fromCity, String toCity, Instant now) {
        Booking booking = new Booking();
        booking.setBookingId(externalId);
        booking.setPnr(pnr);
        booking.setCustomerId(user.getCustomerId());
        booking.setFlightId(flightId);
        booking.setFlightDate(flightDate);
        booking.setTotalPassengers(totalPassengers);
        booking.setBookingType(tripType);
        booking.setTotalFare(amount);
        booking.setTaxCharges(BigDecimal.ZERO);
        booking.setDiscount(BigDecimal.ZERO);
        booking.setFinalAmount(amount);
        booking.setBookingStatus(BookingStatus.CONFIRMED.name());
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        return booking;
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String formatTime(LocalTime time) {
        return time.format(TIME_FMT);
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null) return "—";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + "h " + m + "m";
        if (h > 0) return h + "h";
        return m + "m";
    }

    private String formatFlightStatus(PnrInfoResponse info) {
        StringBuilder paxLines = new StringBuilder();
        for (PnrPassengerDto p : info.passengers()) {
            paxLines.append("\n  - ").append(p.firstName()).append(" ")
                    .append(p.lastName()).append(" (").append(p.passengerType()).append(")");
        }

        String timingLine = "";
        if (info.actualDeparture() != null && !info.actualDeparture().isBlank()) {
            timingLine = "\nActual Departure : " + info.actualDeparture().substring(0, Math.min(16, info.actualDeparture().length()));
        }
        if (info.actualArrival() != null && !info.actualArrival().isBlank()) {
            timingLine += "\nActual Arrival   : " + info.actualArrival().substring(0, Math.min(16, info.actualArrival().length()));
        }

        return """
                Flight Status for PNR %s
                -----------------------------------
                Flight      : %s
                Date        : %s
                From        : %s — %s
                To          : %s — %s
                Departure   : %s
                Arrival     : %s
                Duration    : %s
                Status      : %s%s
                -----------------------------------
                PNR Status  : %s
                Booking     : %s
                Passengers  :%s

                Type 'exit' to return to the main menu.""".formatted(
                info.pnrCode(),
                info.flightId(),
                info.flightDate(),
                info.origin(), info.originAirport(),
                info.destination(), info.destinationAirport(),
                info.departureTime(),
                info.arrivalTime(),
                info.duration(),
                info.liveStatus(),
                timingLine,
                capitalize(info.pnrStatus()),
                capitalize(info.bookingStatus()),
                paxLines.isEmpty() ? " —" : paxLines.toString()
        );
    }

    private String formatWebCheckin(PnrInfoResponse info) {
        if (!"confirmed".equalsIgnoreCase(info.bookingStatus())) {
            return """
                    Web check-in is not available for PNR %s.
                    Booking status is '%s'. Only confirmed bookings are eligible for web check-in.

                    Type 'exit' to return to the main menu.""".formatted(
                    info.pnrCode(),
                    capitalize(info.bookingStatus())
            );
        }

        StringBuilder paxLines = new StringBuilder();
        for (PnrPassengerDto p : info.passengers()) {
            paxLines.append("\n  - ").append(p.firstName()).append(" ")
                    .append(p.lastName()).append(" (").append(p.passengerType()).append(")");
        }

        return """
                Web Check-in Details for PNR %s
                -----------------------------------
                Flight      : %s
                Date        : %s
                From        : %s — %s
                To          : %s — %s
                Departure   : %s
                Arrival     : %s
                -----------------------------------
                Passengers  :%s

                Web check-in opens 48 hours before departure and closes 60 minutes before departure.
                Please visit goindigo.in to complete your check-in.

                Type 'exit' to return to the main menu.""".formatted(
                info.pnrCode(),
                info.flightId(),
                info.flightDate(),
                info.origin(), info.originAirport(),
                info.destination(), info.destinationAirport(),
                info.departureTime(),
                info.arrivalTime(),
                paxLines.isEmpty() ? " —" : paxLines.toString()
        );
    }

    private String resolveGender(PassengerRequestDto passenger) {
        if (passenger.gender() != null && !passenger.gender().isBlank()) {
            return normalizeGender(passenger.gender());
        }
        if (passenger.title() != null && !passenger.title().isBlank()) {
            return TITLE_TO_GENDER.getOrDefault(passenger.title(), "Other");
        }
        return "Other";
    }

    private String normalizeGender(String gender) {
        return switch (gender.trim().toLowerCase()) {
            case "m", "male" -> "Male";
            case "f", "female" -> "Female";
            case "o", "other" -> "Other";
            default -> gender.trim();
        };
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
