package com.examplesubscribemaster.scheduler;

import com.examplesubscribemaster.service.ReminderService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderScheduler {
    private final ReminderService service;

    public PaymentReminderScheduler(ReminderService s) {
        service = s;
    }

    @Scheduled(cron = "0 0 9 * * *")
    @SchedulerLock(name = "paymentReminderScheduler", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void run() {
        service.sendUpcomingPaymentReminders();
    }
}
