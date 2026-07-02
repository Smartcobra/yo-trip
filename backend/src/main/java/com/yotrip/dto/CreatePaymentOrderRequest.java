package com.yotrip.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentOrderRequest(
        @NotNull String bookingId
) {}
