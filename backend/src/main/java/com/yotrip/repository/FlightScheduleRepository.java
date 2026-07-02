package com.yotrip.repository;

import com.yotrip.entity.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, String> {
    List<FlightSchedule> findByOriginAirportCodeAndDestinationAirportCodeAndStatus(
            String origin, String destination, String status);
}
