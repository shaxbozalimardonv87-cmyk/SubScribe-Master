package com.examplesubscribemaster.dto.payment;

import com.examplesubscribemaster.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(Long id, Long subscriptionId, BigDecimal amount, Currency currency, LocalDate paymentDate,
                              LocalDate nextPaymentDate) {
}
