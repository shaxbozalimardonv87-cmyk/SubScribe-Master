package com.examplesubscribemaster.dto.report;


import java.math.BigDecimal;

public record AnnualReportRow(String subscriptionName, BigDecimal monthlyPrice,
                              com.examplesubscribemaster.enums.Currency originalCurrency,
                              BigDecimal baseCurrencyEquivalent, BigDecimal annualTotalExpense) {
}
