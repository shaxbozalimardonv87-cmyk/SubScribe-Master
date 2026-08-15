package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.currency.CurrencyService;
import com.examplesubscribemaster.entity.Subscription;
import com.examplesubscribemaster.repository.PaymentHistoryRepository;
import com.examplesubscribemaster.repository.SubscriptionRepository;
import com.examplesubscribemaster.repository.UserRepository;
import com.examplesubscribemaster.service.AdminStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {
    private static final int TOP_SERVICES_LIMIT = 10;

    private final UserRepository users;
    private final SubscriptionRepository subscriptions;
    private final PaymentHistoryRepository payments;
    private final CurrencyService currency;

    public AdminStatisticsServiceImpl(UserRepository users, SubscriptionRepository subscriptions, PaymentHistoryRepository payments, CurrencyService currency) {
        this.users = users;
        this.subscriptions = subscriptions;
        this.payments = payments;
        this.currency = currency;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOverview getOverview() {
        BigDecimal total = payments.findAll().stream()
                .map(p -> currency.toUzs(p.getAmount(), p.getCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Subscription> active = subscriptions.findAll().stream().filter(s -> !s.isDeleted()).toList();
        List<ServiceUsage> topServices = active.stream()
                .collect(Collectors.groupingBy(Subscription::getName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_SERVICES_LIMIT)
                .map(e -> new ServiceUsage(e.getKey(), e.getValue()))
                .toList();
        return new AdminOverview(users.count(), active.size(), total, topServices);
    }
}
