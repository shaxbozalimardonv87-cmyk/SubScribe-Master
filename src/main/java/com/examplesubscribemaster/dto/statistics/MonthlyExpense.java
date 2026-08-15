package com.examplesubscribemaster.dto.statistics;

import java.math.BigDecimal;

public record MonthlyExpense(String month, BigDecimal amount) {
}
