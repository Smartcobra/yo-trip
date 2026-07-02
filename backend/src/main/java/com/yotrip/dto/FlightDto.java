package com.yotrip.dto;

import java.math.BigDecimal;
import java.util.Map;

public record FlightDto(
        String flightDate,
        String flightStatus,
        Map<String, Object> departure,
        Map<String, Object> arrival,
        Map<String, Object> airline,
        Map<String, Object> flight,
        BigDecimal price
) {}
