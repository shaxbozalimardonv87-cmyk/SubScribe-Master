package com.examplesubscribemaster.repository;

import com.examplesubscribemaster.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
    Page<Subscription> findAllByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Subscription> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    List<Subscription> findByNextPaymentDateAndIsDeletedFalse(LocalDate date);

    Optional<Subscription> findTopByUserIdAndIsDeletedFalseOrderByPriceDesc(Long userId);
}
