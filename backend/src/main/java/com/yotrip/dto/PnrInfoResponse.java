package com.yotrip.dto;

import java.util.List;

public record PnrInfoResponse(
        String pnrCode,
        String flightId,
        String flightDate,
        String origin,
        String originAirport,
        String destination,
        String destinationAirport,
        String departureTime,
        String arrivalTime,
        String duration,
        String liveStatus,
        String actualDeparture,
        String actualArrival,
        String pnrStatus,
        String bookingStatus,
        List<PnrPassengerDto> passengers
) {}
