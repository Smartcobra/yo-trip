package com.yotrip.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "airline_bookings")
public class Booking {

    @Id
    @Column(name = "booking_id", length = 20)
    private String bookingId;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pnr_code")
    private Pnr pnr;

    @Column(name = "flight_id", length = 10)
    private String flightId;

    @Column(name = "flight_date")
    private LocalDate flightDate;

    @Column(name = "total_passengers")
    private Integer totalPassengers;

    @Column(name = "booking_type", length = 50)
    private String bookingType;

    @Column(name = "total_fare", precision = 10, scale = 2)
    private BigDecimal totalFare;

    @Column(name = "tax_charges", precision = 10, scale = 2)
    private BigDecimal taxCharges = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "booking_status", length = 50)
    private String bookingStatus;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Pnr getPnr() {
        return pnr;
    }

    public void setPnr(Pnr pnr) {
        this.pnr = pnr;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(LocalDate flightDate) {
        this.flightDate = flightDate;
    }

    public Integer getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(Integer totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public BigDecimal getTaxCharges() {
        return taxCharges;
    }

    public void setTaxCharges(BigDecimal taxCharges) {
        this.taxCharges = taxCharges;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExternalBookingId() {
        return bookingId;
    }

    public void setExternalBookingId(String externalBookingId) {
        this.bookingId = externalBookingId;
    }

    public String getTripType() {
        return bookingType;
    }

    public void setTripType(String tripType) {
        this.bookingType = tripType;
    }

    public BigDecimal getBaseFare() {
        return totalFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.totalFare = baseFare;
    }

    public BigDecimal getSurcharges() {
        return taxCharges;
    }

    public void setSurcharges(BigDecimal surcharges) {
        this.taxCharges = surcharges;
    }

    public BigDecimal getTotalAmount() {
        return finalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.finalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        if (bookingStatus == null) {
            return BookingStatus.DRAFT;
        }
        try {
            return BookingStatus.valueOf(bookingStatus.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return BookingStatus.DRAFT;
        }
    }

    public void setStatus(BookingStatus status) {
        this.bookingStatus = status.name();
    }
}
