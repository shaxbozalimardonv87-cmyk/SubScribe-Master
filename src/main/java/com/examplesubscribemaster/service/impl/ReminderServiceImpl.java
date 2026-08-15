package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.repository.SubscriptionRepository;
import com.examplesubscribemaster.service.ReminderService;
import com.examplesubscribemaster.service.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReminderServiceImpl implements ReminderService {
    private final SubscriptionRepository repo;
    private final NotificationService notifier;

    public ReminderServiceImpl(SubscriptionRepository r, NotificationService n) {
        repo = r;
        notifier = n;
    }

    public void sendUpcomingPaymentReminders() {
        repo.findByNextPaymentDateAndIsDeletedFalse(LocalDate.now().plusDays(2)).forEach(s -> notifier.notifyUser(s.getUser().getEmail(), "Upcoming subscription payment", "Payment for " + s.getName() + " is due on " + s.getNextPaymentDate()));
    }
}
