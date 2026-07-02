package com.yotrip.controller;

import com.yotrip.dto.*;
import com.yotrip.entity.User;
import com.yotrip.service.BookingService;
import com.yotrip.service.PnrService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Bookings", description = "Bookings and coupons")
public class BookingController {

    private final BookingService bookingService;
    private final PnrService pnrService;

    public BookingController(BookingService bookingService, PnrService pnrService) {
        this.bookingService = bookingService;
        this.pnrService = pnrService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(user, request));
    }

    @PostMapping("/bookings/complete")
    @SecurityRequirements
    public ResponseEntity<CompleteBookingResponse> completeBooking(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CompleteBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pnrService.completeBooking(request, user));
    }

    @GetMapping("/bookings")
    public List<BookingResponse> getBookings(@AuthenticationPrincipal User user) {
        return bookingService.getUserBookings(user);
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingResponse getBooking(@AuthenticationPrincipal User user, @PathVariable String bookingId) {
        return bookingService.getBooking(user, bookingId);
    }

    @GetMapping("/coupons/validate")
    @SecurityRequirements
    public CouponResponse validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {
        return bookingService.validateCoupon(code, amount);
    }
}
