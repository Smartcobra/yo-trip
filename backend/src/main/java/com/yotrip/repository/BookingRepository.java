package com.yotrip.repository;

import com.yotrip.entity.Booking;
import com.yotrip.entity.Pnr;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Booking> findByPnrOrderByCreatedAtAsc(Pnr pnr);
    Optional<Booking> findFirstByPnrOrderByCreatedAtAsc(Pnr pnr);
}
