package com.examplesubscribemaster.service;

import java.math.BigDecimal;
import java.util.List;

public interface AdminStatisticsService {
    AdminOverview getOverview();

    record AdminOverview(long users, long subscriptions, BigDecimal totalPaidUzs, List<ServiceUsage> topServices) {
    }

    record ServiceUsage(String name, long subscribers) {
    }
}
