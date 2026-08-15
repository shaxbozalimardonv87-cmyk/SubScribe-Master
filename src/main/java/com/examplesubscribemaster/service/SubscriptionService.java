package com.examplesubscribemaster.service;

import com.examplesubscribemaster.dto.subscription.*;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {
    SubscriptionResponse create(Long userId, SubscriptionRequest r);

    PageResponse<SubscriptionResponse> findAll(Long userId, SubscriptionStatus status, Currency currency, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);

    SubscriptionResponse find(Long userId, Long id);

    SubscriptionResponse update(Long userId, Long id, SubscriptionRequest r);

    void delete(Long userId, Long id);
}
