package com.yotrip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public record CreateBookingRequest(
        @NotNull Map<String, Object> flightData,
        @NotBlank String fromIata,
        @NotBlank String toIata,
        @NotNull BigDecimal baseFare,
        @NotNull BigDecimal surcharges,
        String couponCode,
        String travellerName,
        String travellerEmail,
        String travellerPhone,
        String gstNumber
) {}
