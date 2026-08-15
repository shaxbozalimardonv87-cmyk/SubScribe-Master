package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.payment.PaymentRequest;
import com.examplesubscribemaster.dto.payment.PaymentResponse;
import com.examplesubscribemaster.entity.PaymentHistory;
import com.examplesubscribemaster.entity.Subscription;
import com.examplesubscribemaster.entity.User;
import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.exception.BusinessException;
import com.examplesubscribemaster.exception.ResourceNotFoundException;
import com.examplesubscribemaster.repository.PaymentHistoryRepository;
import com.examplesubscribemaster.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private SubscriptionRepository subscriptions;
    @Mock private PaymentHistoryRepository payments;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaymentServiceImpl(subscriptions, payments);
    }

    private Subscription subscription(SubscriptionStatus status) {
        User owner = new User("user@example.com", "hash", Role.USER);
        return new Subscription("Spotify", new BigDecimal("5.00"), Currency.USD, LocalDate.of(2026, 1, 1),
            Periodicity.MONTHLY, status, Category.ENTERTAINMENT, owner);
    }

    @Test
    void pay_activeSubscription_recordsHistoryAndAdvancesNextPaymentDate() {
        Subscription sub = subscription(SubscriptionStatus.ACTIVE);
        LocalDate before = sub.getNextPaymentDate();
        when(subscriptions.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(sub));
        when(payments.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = service.pay(1L, 1L, new PaymentRequest(new BigDecimal("5.00"), LocalDate.of(2026, 2, 1)));

        assertThat(response.amount()).isEqualByComparingTo("5.00");
        assertThat(sub.getNextPaymentDate()).isEqualTo(before.plusMonths(1));
        assertThat(response.nextPaymentDate()).isEqualTo(before.plusMonths(1));
        verify(payments).save(any(PaymentHistory.class));
    }

    @Test
    void pay_cancelledSubscription_throwsBusinessException() {
        Subscription sub = subscription(SubscriptionStatus.CANCELLED);
        when(subscriptions.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> service.pay(1L, 1L, new PaymentRequest(new BigDecimal("5.00"), LocalDate.now())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cancelled");
        verify(payments, never()).save(any());
    }

    @Test
    void pay_pausedSubscription_isStillPayable() {
        Subscription sub = subscription(SubscriptionStatus.PAUSED);
        when(subscriptions.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(sub));
        when(payments.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = service.pay(1L, 1L, new PaymentRequest(new BigDecimal("5.00"), LocalDate.now()));

        assertThat(response).isNotNull();
        verify(payments).save(any(PaymentHistory.class));
    }

    @Test
    void pay_subscriptionNotOwnedOrMissing_throwsResourceNotFound() {
        when(subscriptions.findByIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pay(2L, 1L, new PaymentRequest(new BigDecimal("5.00"), LocalDate.now())))
            .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(payments);
    }
}
