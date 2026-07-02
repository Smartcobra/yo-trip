package com.yotrip.dto;

import java.math.BigDecimal;

public record SimpleFlightDto(
        String flightNumber,
        String departureTime,
        String arrivalTime,
        String duration,
        BigDecimal price,
        String origin,
        String destination
) {}
