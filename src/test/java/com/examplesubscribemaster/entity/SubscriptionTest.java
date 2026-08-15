package com.examplesubscribemaster.entity;

import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionTest {

    private User owner() {
        return new User("owner@example.com", "hash", Role.USER);
    }

    @Test
    void constructor_computesNextPaymentDateOnePeriodAfterStartDate() {
        LocalDate start = LocalDate.of(2026, 1, 15);
        Subscription sub = new Subscription("Netflix", new BigDecimal("10.00"), Currency.USD, start,
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, owner());

        assertThat(sub.getNextPaymentDate()).isEqualTo(start.plusMonths(1));
        assertThat(sub.isDeleted()).isFalse();
    }

    @Test
    void markPaid_advancesNextPaymentDateByOnePeriod() {
        Subscription sub = new Subscription("Netflix", new BigDecimal("10.00"), Currency.USD, LocalDate.of(2026, 1, 15),
            Periodicity.WEEKLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, owner());
        LocalDate before = sub.getNextPaymentDate();

        sub.markPaid();

        assertThat(sub.getNextPaymentDate()).isEqualTo(before.plusWeeks(1));
    }

    @Test
    void update_recalculatesNextPaymentDateFromNewStartDateAndPeriodicity() {
        Subscription sub = new Subscription("Netflix", new BigDecimal("10.00"), Currency.USD, LocalDate.of(2026, 1, 15),
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, owner());
        LocalDate newStart = LocalDate.of(2026, 3, 1);

        sub.update("Netflix Premium", new BigDecimal("15.00"), Currency.EUR, newStart, Periodicity.YEARLY,
            SubscriptionStatus.PAUSED, Category.PRODUCTIVITY);

        assertThat(sub.getName()).isEqualTo("Netflix Premium");
        assertThat(sub.getPrice()).isEqualByComparingTo("15.00");
        assertThat(sub.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.PAUSED);
        assertThat(sub.getCategory()).isEqualTo(Category.PRODUCTIVITY);
        assertThat(sub.getNextPaymentDate()).isEqualTo(newStart.plusYears(1));
    }

    @Test
    void softDelete_flagsRecordWithoutClearingData() {
        Subscription sub = new Subscription("Netflix", new BigDecimal("10.00"), Currency.USD, LocalDate.of(2026, 1, 15),
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, owner());

        sub.softDelete();

        assertThat(sub.isDeleted()).isTrue();
        assertThat(sub.getName()).isEqualTo("Netflix");
    }
}
