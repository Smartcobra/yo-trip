package com.yotrip.dto;

import java.util.List;

public record FlightSearchResponse(
        List<SimpleFlightDto> flights,
        String message,
        int flightsFound,
        String route,
        String travelDate
) {}
