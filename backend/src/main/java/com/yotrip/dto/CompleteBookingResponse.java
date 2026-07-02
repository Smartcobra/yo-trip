package com.yotrip.dto;

public record CompleteBookingResponse(
        String pnrCode,
        String transactionId,
        String bookingId
) {}
