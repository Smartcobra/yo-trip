package com.yotrip.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "passengers")
public class Passenger {

    @Id
    @Column(length = 20)
    private String passengerId;

    @Column(name = "booking_id", nullable = false, length = 20)
    private String bookingId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false, length = 10)
    private String passengerType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
