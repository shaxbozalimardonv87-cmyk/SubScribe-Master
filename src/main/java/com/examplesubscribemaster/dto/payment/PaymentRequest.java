package com.examplesubscribemaster.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(@NotNull @Positive BigDecimal amount, @NotNull LocalDate paymentDate) {
}
