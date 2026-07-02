package com.yotrip.dto;

import java.math.BigDecimal;

public record SelectedFlightDto(
        String flightNumber,
        BigDecimal price
) {}
