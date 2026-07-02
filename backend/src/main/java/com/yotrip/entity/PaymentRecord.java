package com.yotrip.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "airline_payments")
public class PaymentRecord {

    @Id
    @Column(name = "payment_id", length = 20)
    private String paymentId;

    @Column(name = "booking_id", length = 20, nullable = false)
    private String bookingId;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Transient
    private String razorpayOrderId;

    @Transient
    private String razorpayPaymentId;

    @Transient
    private String razorpaySignature;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
        if (this.transactionId == null || this.transactionId.isBlank()) {
            this.transactionId = razorpayOrderId;
        }
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public PaymentStatus getStatus() {
        if (paymentStatus == null) {
            return PaymentStatus.CREATED;
        }
        try {
            return PaymentStatus.valueOf(paymentStatus.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PaymentStatus.CREATED;
        }
    }

    public void setStatus(PaymentStatus status) {
        this.paymentStatus = status.name();
    }

    public void setBooking(Booking booking) {
        this.bookingId = booking.getBookingId();
    }
}
