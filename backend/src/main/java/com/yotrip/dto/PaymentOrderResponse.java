package com.yotrip.dto;

public record PaymentOrderResponse(
        String orderId,
        String currency,
        long amount,
        String keyId,
        String bookingId,
        boolean demoMode
) {}
