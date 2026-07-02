package com.yotrip.controller;

import com.yotrip.dto.FlightDto;
import com.yotrip.dto.FlightSearchResponse;
import com.yotrip.service.FlightService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@Tag(name = "Flights", description = "Flight search")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/search")
    @SecurityRequirements
    public Object search(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "price_asc") String sort,
            @RequestParam(defaultValue = "standard") String format,
            @RequestParam(required = false) String bookingLeg) {
        if ("bot".equalsIgnoreCase(format)) {
            return flightService.searchFlightsBotFormat(from, to, date, bookingLeg);
        }
        return flightService.searchFlights(from, to, sort, date);
    }
}
