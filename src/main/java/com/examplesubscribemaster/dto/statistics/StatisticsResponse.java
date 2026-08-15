package com.examplesubscribemaster.dto.statistics;

import com.examplesubscribemaster.dto.subscription.SubscriptionResponse;

import java.math.BigDecimal;
import java.util.List;

public record StatisticsResponse(SubscriptionResponse mostExpensive, BigDecimal currentMonthTotal,
                                 BigDecimal currentMonthBaseCurrencyTotal, List<MonthlyExpense> monthlyTrend,
                                 List<CategoryExpense> byCategory) {
}
