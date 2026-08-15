package com.examplesubscribemaster.dto.subscription;

import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponse(Long id, String name, BigDecimal price, Currency currency, LocalDate startDate,
                                   LocalDate nextPaymentDate, Periodicity periodicity, SubscriptionStatus status,
                                   Category category, Long version) {
}
