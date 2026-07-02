package com.yotrip.repository;

import com.yotrip.entity.OtpSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpSessionRepository extends JpaRepository<OtpSession, Long> {
    Optional<OtpSession> findTopByMobileOrderByIdDesc(String mobile);
    void deleteByMobile(String mobile);
}
