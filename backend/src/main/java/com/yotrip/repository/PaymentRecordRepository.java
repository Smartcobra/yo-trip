package com.yotrip.repository;

import com.yotrip.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByTransactionId(String transactionId);
}
