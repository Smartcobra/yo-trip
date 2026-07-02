package com.yotrip.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_schedule")
public class FlightSchedule {

    @Id
    @Column(length = 10)
    private String flightId;

    @Column(nullable = false, length = 3)
    private String originAirportCode;

    @Column(nullable = false, length = 3)
    private String destinationAirportCode;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    @Column(nullable = false)
    private Integer flightDurationMinutes;

    private Integer distanceKm;

    @Column(length = 20)
    private String aircraftType;

    private Integer seatCapacity;

    @Column(length = 50)
    private String airlineCode = "6E";

    @Column(length = 100)
    private String airlineName = "IndiGo";

    @Column(length = 50)
    private String status = "active";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "flightSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DaysOfOperation> daysOfOperation = new ArrayList<>();

    public void addDayOfOperation(DaysOfOperation day) {
        daysOfOperation.add(day);
        day.setFlightSchedule(this);
    }

    public List<DaysOfOperation> getDaysOfOperation() {
        return daysOfOperation;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getOriginAirportCode() {
        return originAirportCode;
    }

    public void setOriginAirportCode(String originAirportCode) {
        this.originAirportCode = originAirportCode;
    }

    public String getDestinationAirportCode() {
        return destinationAirportCode;
    }

    public void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getFlightDurationMinutes() {
        return flightDurationMinutes;
    }

    public void setFlightDurationMinutes(Integer flightDurationMinutes) {
        this.flightDurationMinutes = flightDurationMinutes;
    }

    public Integer getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public Integer getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(Integer seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public String getAirlineName() {
        return airlineName;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
