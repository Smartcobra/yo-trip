package com.yotrip.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record BookingResponse(
        String bookingId,
        String status,
        String fromIata,
        String toIata,
        BigDecimal baseFare,
        BigDecimal surcharges,
        BigDecimal discount,
        BigDecimal totalAmount,
        String couponCode,
        String travellerName,
        String travellerEmail,
        String travellerPhone,
        String gstNumber,
        Map<String, Object> flightData,
        Instant createdAt
) {}
