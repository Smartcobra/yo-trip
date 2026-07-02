package com.yotrip.controller;

import com.yotrip.dto.*;
import com.yotrip.entity.Booking;
import com.yotrip.entity.User;
import com.yotrip.service.BookingService;
import com.yotrip.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Razorpay payment flow")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/create-order")
    public PaymentOrderResponse createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePaymentOrderRequest request) {
        return paymentService.createOrder(user, request.bookingId());
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifyPaymentRequest request) {
        Booking booking = paymentService.confirmPayment(user, request);
        return ResponseEntity.ok(Map.of(
                "message", "Payment successful",
                "booking", bookingService.getBooking(user, booking.getBookingId())
        ));
    }
}
