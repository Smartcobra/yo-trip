package com.yotrip.service;

import com.yotrip.config.AppProperties;
import com.yotrip.dto.PaymentOrderResponse;
import com.yotrip.dto.VerifyPaymentRequest;
import com.yotrip.entity.Booking;
import com.yotrip.entity.PaymentRecord;
import com.yotrip.entity.PaymentStatus;
import com.yotrip.entity.User;
import com.yotrip.exception.BadRequestException;
import com.yotrip.repository.PaymentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final RazorpayClient razorpayClient;
    private final AppProperties appProperties;

    public PaymentService(PaymentRecordRepository paymentRecordRepository,
                          BookingService bookingService,
                          EmailService emailService,
                          RazorpayClient razorpayClient,
                          AppProperties appProperties) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.bookingService = bookingService;
        this.emailService = emailService;
        this.razorpayClient = razorpayClient;
        this.appProperties = appProperties;
    }

    @Transactional
    public PaymentOrderResponse createOrder(User user, String bookingId) {
        Booking booking = bookingService.getBookingEntity(user, bookingId);
        if (booking.getStatus() != com.yotrip.entity.BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not eligible for payment");
        }

        long amountPaise = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        String orderId;
        boolean demoMode = false;
        try {
            orderId = razorpayClient.createOrder(amountPaise, booking.getBookingId());
        } catch (BadRequestException ex) {
            if (!appProperties.getRazorpay().isTestMode()) {
                throw ex;
            }
            orderId = "order_demo_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            demoMode = true;
        }

        PaymentRecord payment = new PaymentRecord();
        payment.setPaymentId("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        payment.setBookingId(booking.getBookingId());
        payment.setRazorpayOrderId(orderId);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.CREATED.name());
        payment.setPaymentGateway(demoMode ? "yo-trip-demo" : "Razorpay");
        payment.setCreatedAt(Instant.now());
        paymentRecordRepository.save(payment);

        String keyId = appProperties.getRazorpay().getKeyId();
        if (keyId == null || keyId.isBlank()) {
            keyId = appProperties.getRazorpay().isTestMode() ? "rzp_test_demo" : "";
            if (keyId.isBlank()) {
                throw new BadRequestException("Razorpay key ID is not configured");
            }
        }

        return new PaymentOrderResponse(orderId, "INR", amountPaise, keyId, booking.getBookingId(), demoMode);
    }

    @Transactional
    public Booking confirmPayment(User user, VerifyPaymentRequest request) {
        Booking booking = bookingService.getBookingEntity(user, request.bookingId());
        PaymentRecord payment = paymentRecordRepository.findByTransactionId(request.razorpayOrderId())
                .orElseThrow(() -> new BadRequestException("Payment order not found"));

        if (!payment.getBookingId().equals(booking.getBookingId())) {
            throw new BadRequestException("Payment does not match booking");
        }

        if (!verifySignature(request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature())) {
            payment.setPaymentStatus(PaymentStatus.FAILED.name());
            paymentRecordRepository.save(payment);
            throw new BadRequestException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setRazorpaySignature(request.razorpaySignature());
        payment.setPaymentStatus(PaymentStatus.SUCCESS.name());
        paymentRecordRepository.save(payment);

        bookingService.confirmBooking(booking);
        emailService.sendBookingConfirmation(user, booking, request.razorpayPaymentId());
        return booking;
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        if (appProperties.getRazorpay().isTestMode()) {
            return true;
        }
        String secret = appProperties.getRazorpay().getKeySecret();
        if (secret == null || secret.isBlank()) {
            return false;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
