package com.examplesubscribemaster.dto.statistics;

import com.examplesubscribemaster.enums.Category;

import java.math.BigDecimal;

public record CategoryExpense(Category category, BigDecimal amount) {
}
