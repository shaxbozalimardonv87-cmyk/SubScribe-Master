package com.examplesubscribemaster.repository;

import com.examplesubscribemaster.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    List<PaymentHistory> findBySubscriptionUserIdAndPaymentDateBetween(Long userId, LocalDate from, LocalDate to);

    List<PaymentHistory> findBySubscriptionUserIdAndPaymentDateBetweenAndSubscriptionIsDeletedFalse(Long userId, LocalDate from, LocalDate to);
}
