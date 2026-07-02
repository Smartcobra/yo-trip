package com.yotrip.repository;

import com.yotrip.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, String> {
    List<Passenger> findByBookingId(String bookingId);
}
