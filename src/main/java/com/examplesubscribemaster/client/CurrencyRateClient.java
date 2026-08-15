package com.examplesubscribemaster.client;

import com.examplesubscribemaster.enums.Currency;

import java.math.BigDecimal;
import java.util.Map;

public interface CurrencyRateClient {
    Map<Currency, BigDecimal> getRatesToUzs();
}
