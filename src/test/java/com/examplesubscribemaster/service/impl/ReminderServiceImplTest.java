package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.entity.Subscription;
import com.examplesubscribemaster.entity.User;
import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.repository.SubscriptionRepository;
import com.examplesubscribemaster.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock private SubscriptionRepository repo;
    @Mock private NotificationService notifier;

    private ReminderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReminderServiceImpl(repo, notifier);
    }

    private Subscription subscription(String email, String name) {
        User owner = new User(email, "hash", Role.USER);
        return new Subscription(name, new BigDecimal("9.99"), Currency.USD, LocalDate.of(2026, 1, 1),
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, owner);
    }

    @Test
    void sendUpcomingPaymentReminders_notifiesEachDueSubscriptionOwner() {
        Subscription first = subscription("alice@example.com", "Netflix");
        Subscription second = subscription("bob@example.com", "Spotify");
        when(repo.findByNextPaymentDateAndIsDeletedFalse(LocalDate.now().plusDays(2)))
            .thenReturn(List.of(first, second));

        service.sendUpcomingPaymentReminders();

        verify(notifier).notifyUser(eq("alice@example.com"), eq("Upcoming subscription payment"), contains("Netflix"));
        verify(notifier).notifyUser(eq("bob@example.com"), eq("Upcoming subscription payment"), contains("Spotify"));
    }

    @Test
    void sendUpcomingPaymentReminders_noDueSubscriptions_sendsNoNotifications() {
        when(repo.findByNextPaymentDateAndIsDeletedFalse(LocalDate.now().plusDays(2))).thenReturn(List.of());

        service.sendUpcomingPaymentReminders();

        verifyNoInteractions(notifier);
    }
}
