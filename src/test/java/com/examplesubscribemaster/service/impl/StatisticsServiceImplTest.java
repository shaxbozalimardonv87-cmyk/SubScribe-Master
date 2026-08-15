package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.currency.CurrencyService;
import com.examplesubscribemaster.dto.statistics.StatisticsResponse;
import com.examplesubscribemaster.dto.subscription.SubscriptionResponse;
import com.examplesubscribemaster.entity.PaymentHistory;
import com.examplesubscribemaster.entity.Subscription;
import com.examplesubscribemaster.entity.User;
import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.mapper.SubscriptionMapper;
import com.examplesubscribemaster.repository.PaymentHistoryRepository;
import com.examplesubscribemaster.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock private SubscriptionRepository subs;
    @Mock private PaymentHistoryRepository payments;
    @Mock private CurrencyService currency;
    @Mock private SubscriptionMapper mapper;

    private StatisticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StatisticsServiceImpl(subs, payments, currency, mapper);
    }

    private Subscription subscription(Category category) {
        User owner = new User("user@example.com", "hash", Role.USER);
        return new Subscription("Sub", new BigDecimal("1.00"), Currency.USD, LocalDate.of(2026, 1, 1),
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, category, owner);
    }

    @Test
    void get_aggregatesCurrentMonthTotalsTrendAndCategoryBreakdown() {
        when(currency.toUzs(any(BigDecimal.class), any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));
        Subscription entertainmentSub = subscription(Category.ENTERTAINMENT);
        Subscription productivitySub = subscription(Category.PRODUCTIVITY);
        LocalDate thisMonth = LocalDate.now();
        LocalDate lastMonth = thisMonth.minusMonths(1);
        PaymentHistory currentMonthPayment = new PaymentHistory(entertainmentSub, new BigDecimal("100"), Currency.USD, thisMonth);
        PaymentHistory previousMonthPayment = new PaymentHistory(productivitySub, new BigDecimal("50"), Currency.EUR, lastMonth);

        when(subs.findTopByUserIdAndIsDeletedFalseOrderByPriceDesc(1L)).thenReturn(Optional.of(entertainmentSub));
        SubscriptionResponse mapped = new SubscriptionResponse(1L, "Sub", entertainmentSub.getPrice(), Currency.USD,
            entertainmentSub.getStartDate(), entertainmentSub.getNextPaymentDate(), Periodicity.MONTHLY,
            SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, 0L);
        when(mapper.toResponse(entertainmentSub)).thenReturn(mapped);
        when(payments.findBySubscriptionUserIdAndPaymentDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(currentMonthPayment, previousMonthPayment));

        StatisticsResponse response = service.get(1L);

        assertThat(response.mostExpensive()).isEqualTo(mapped);
        assertThat(response.currentMonthTotal()).isEqualByComparingTo("100");
        assertThat(response.currentMonthBaseCurrencyTotal()).isEqualByComparingTo("100");
        assertThat(response.monthlyTrend()).hasSize(2);
        assertThat(response.byCategory()).hasSize(2);
        assertThat(response.byCategory())
            .anySatisfy(c -> {
                if (c.category() == Category.ENTERTAINMENT) assertThat(c.amount()).isEqualByComparingTo("100");
            })
            .anySatisfy(c -> {
                if (c.category() == Category.PRODUCTIVITY) assertThat(c.amount()).isEqualByComparingTo("50");
            });
    }

    @Test
    void get_noSubscriptions_returnsNullMostExpensiveAndZeroTotals() {
        when(subs.findTopByUserIdAndIsDeletedFalseOrderByPriceDesc(1L)).thenReturn(Optional.empty());
        when(payments.findBySubscriptionUserIdAndPaymentDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());

        StatisticsResponse response = service.get(1L);

        assertThat(response.mostExpensive()).isNull();
        assertThat(response.currentMonthTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.byCategory()).isEmpty();
        assertThat(response.monthlyTrend()).isEmpty();
    }
}
