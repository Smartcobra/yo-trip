package com.yotrip.service;

import com.yotrip.dto.FlightDto;
import com.yotrip.dto.FlightSearchResponse;
import com.yotrip.dto.SimpleFlightDto;
import com.yotrip.entity.FlightSchedule;
import com.yotrip.repository.FlightScheduleRepository;
import com.yotrip.util.CityIataResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class FlightService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DATE_DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final FlightScheduleRepository flightScheduleRepository;

    public FlightService(FlightScheduleRepository flightScheduleRepository) {
        this.flightScheduleRepository = flightScheduleRepository;
    }

    public List<FlightDto> searchFlights(String from, String to, String sort) {
        return searchFlights(from, to, sort, null);
    }

    @Transactional(readOnly = true)
    public List<FlightDto> searchFlights(String from, String to, String sort, String date) {
        String fromIata = CityIataResolver.resolveToIata(from);
        String toIata = CityIataResolver.resolveToIata(to);
        LocalDate searchDate = parseDate(date);

        List<FlightDto> flights = flightScheduleRepository
                .findByOriginAirportCodeAndDestinationAirportCodeAndStatus(
                        fromIata, toIata, "active")
                .stream()
                .filter(schedule -> operatesOnDate(schedule, searchDate))
                .map(schedule -> toDto(schedule, searchDate))
                .toList();

        List<FlightDto> sorted = new ArrayList<>(flights);
        if ("price_desc".equalsIgnoreCase(sort)) {
            sorted.sort(Comparator.comparing(FlightDto::price).reversed());
        } else {
            sorted.sort(Comparator.comparing(FlightDto::price));
        }
        return sorted;
    }

    @Transactional(readOnly = true)
    public FlightSearchResponse searchFlightsBotFormat(String from, String to, String date, String bookingLeg) {
        String departure = from;
        String destination = to;
        if ("return".equalsIgnoreCase(bookingLeg)) {
            departure = to;
            destination = from;
        }

        String fromIata = CityIataResolver.resolveToIata(departure);
        String toIata = CityIataResolver.resolveToIata(destination);
        LocalDate searchDate = parseDate(date);

        List<SimpleFlightDto> flights = flightScheduleRepository
                .findByOriginAirportCodeAndDestinationAirportCodeAndStatus(
                        fromIata, toIata, "active")
                .stream()
                .filter(schedule -> operatesOnDate(schedule, searchDate))
                .map(this::toSimpleDto)
                .sorted(Comparator.comparing(SimpleFlightDto::price))
                .toList();

        String route = departure + " → " + destination;
        String dateDisplay = formatDateDisplay(searchDate);
        String legLabel = "return".equalsIgnoreCase(bookingLeg) ? "return flights" : "flights";

        if (flights.isEmpty()) {
            return new FlightSearchResponse(
                    List.of(),
                    "Sorry, no flights found for that route and date. Please try a different date or route.",
                    0,
                    route,
                    searchDate.toString()
            );
        }

        StringBuilder text = new StringBuilder();
        text.append("Congratulations! You are receiving a discounted fare with IndiGo's 6Exclusive offer.\n\n");
        text.append("Available ").append(legLabel).append(" on ").append(dateDisplay)
                .append(" (").append(route).append("):\n\n");

        for (int i = 0; i < flights.size(); i++) {
            SimpleFlightDto f = flights.get(i);
            text.append("-----\nFlight ").append(i + 1).append("\n");
            text.append(f.departureTime()).append(" -- ").append(f.duration())
                    .append(" -- ").append(f.arrivalTime()).append("\n");
            text.append("Starts at Rs.").append(f.price()).append("\n");
            text.append("Non-stop\n");
            text.append(f.flightNumber()).append("\n");
            text.append("-----\n\n");
        }
        text.append("Please choose the flight you wish to book.\neg. flight 1, cheapest flight, 9:00 AM");

        return new FlightSearchResponse(flights, text.toString(), flights.size(), route, searchDate.toString());
    }

    private FlightDto toDto(FlightSchedule schedule, LocalDate flightDate) {
        String dateStr = flightDate.toString();
        BigDecimal price = priceFromDistance(schedule.getDistanceKm());

        Map<String, Object> departure = Map.of(
                "airport", schedule.getOriginAirportCode(),
                "iata", schedule.getOriginAirportCode(),
                "scheduled", dateStr + "T" + schedule.getDepartureTime() + ":00+00:00"
        );
        Map<String, Object> arrival = Map.of(
                "airport", schedule.getDestinationAirportCode(),
                "iata", schedule.getDestinationAirportCode(),
                "scheduled", dateStr + "T" + schedule.getArrivalTime() + ":00+00:00"
        );
        Map<String, Object> airline = Map.of(
                "name", schedule.getAirlineName(),
                "iata", schedule.getAirlineCode()
        );
        Map<String, Object> flight = Map.of(
                "iata", schedule.getFlightId(),
                "number", schedule.getFlightId().replace("6E", "")
        );

        return new FlightDto(dateStr, "scheduled", departure, arrival, airline, flight, price);
    }

    private SimpleFlightDto toSimpleDto(FlightSchedule schedule) {
        return new SimpleFlightDto(
                schedule.getFlightId(),
                formatTime(schedule.getDepartureTime()),
                formatTime(schedule.getArrivalTime()),
                formatDuration(schedule.getFlightDurationMinutes()),
                priceFromDistance(schedule.getDistanceKm()),
                schedule.getOriginAirportCode(),
                schedule.getDestinationAirportCode()
        );
    }

    private boolean operatesOnDate(FlightSchedule schedule, LocalDate date) {
        if (schedule.getDaysOfOperation() == null || schedule.getDaysOfOperation().isEmpty()) {
            return true;
        }
        int dayOfWeek = date.getDayOfWeek().getValue();
        return schedule.getDaysOfOperation().stream().anyMatch(day ->
                day.getDayOfWeek() == dayOfWeek
                        && !date.isBefore(day.getEffectiveFrom())
                        && !date.isAfter(day.getEffectiveTo())
        );
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String formatDateDisplay(LocalDate date) {
        return date.format(DATE_DISPLAY);
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

    private BigDecimal priceFromDistance(Integer distanceKm) {
        if (distanceKm == null || distanceKm == 0) {
            return BigDecimal.valueOf(3500);
        }
        return BigDecimal.valueOf(Math.max(1500, distanceKm * 8L));
    }
}
