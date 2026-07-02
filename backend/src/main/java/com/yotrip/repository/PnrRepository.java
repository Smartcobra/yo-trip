package com.yotrip.repository;

import com.yotrip.entity.Pnr;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PnrRepository extends JpaRepository<Pnr, String> {
    Optional<Pnr> findByPnrCode(String pnrCode);
    boolean existsByPnrCode(String pnrCode);
}
