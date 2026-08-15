package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.statistics.*;
import com.examplesubscribemaster.entity.*;
import com.examplesubscribemaster.currency.CurrencyService;
import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.mapper.SubscriptionMapper;
import com.examplesubscribemaster.repository.*;
import com.examplesubscribemaster.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final SubscriptionRepository subs;
    private final PaymentHistoryRepository payments;
    private final CurrencyService currency;
    private final SubscriptionMapper mapper;

    public StatisticsServiceImpl(SubscriptionRepository s, PaymentHistoryRepository p, CurrencyService c, SubscriptionMapper m) {
        subs = s;
        payments = p;
        currency = c;
        mapper = m;
    }

    @Transactional(readOnly = true)
    public StatisticsResponse get(Long u, int months) {
        int span = months < 1 ? 1 : months;
        var expensive = subs.findTopByUserIdAndIsDeletedFalseOrderByPriceDesc(u).map(mapper::toResponse).orElse(null);
        LocalDate now = LocalDate.now(), from = now.withDayOfMonth(1), to = now.withDayOfMonth(now.lengthOfMonth());
        var ps = payments.findBySubscriptionUserIdAndPaymentDateBetween(u, now.minusMonths(span - 1).withDayOfMonth(1), to);
        BigDecimal total = ps.stream().filter(p -> !p.getPaymentDate().isBefore(from) && !p.getPaymentDate().isAfter(to)).map(PaymentHistory::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal base = ps.stream().filter(p -> !p.getPaymentDate().isBefore(from) && !p.getPaymentDate().isAfter(to)).map(p -> currency.toUzs(p.getAmount(), p.getCurrency())).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> trend = ps.stream().collect(Collectors.groupingBy(p -> p.getPaymentDate().toString().substring(0, 7), TreeMap::new, Collectors.mapping(p -> currency.toUzs(p.getAmount(), p.getCurrency()), Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        List<MonthlyExpense> mt = trend.entrySet().stream().map(e -> new MonthlyExpense(e.getKey(), e.getValue())).toList();
        Map<Category, BigDecimal> cats = new EnumMap<>(Category.class);
        ps.forEach(p -> cats.merge(p.getSubscription().getCategory(), currency.toUzs(p.getAmount(), p.getCurrency()), BigDecimal::add));
        return new StatisticsResponse(expensive, total, base, mt, cats.entrySet().stream().map(e -> new CategoryExpense(e.getKey(), e.getValue())).toList());
    }
}
