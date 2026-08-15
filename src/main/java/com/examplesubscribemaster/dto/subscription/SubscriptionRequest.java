package com.examplesubscribemaster.dto.subscription;

import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRequest(@NotBlank @Size(max = 120) String name, @NotNull @Positive BigDecimal price,
                                  @NotNull Currency currency, @NotNull LocalDate startDate,
                                  @NotNull Periodicity periodicity, @NotNull SubscriptionStatus status,
                                  @NotNull Category category) {
}
