package com.examplesubscribemaster.currency;

import com.examplesubscribemaster.client.CurrencyRateClient;
import com.examplesubscribemaster.enums.Currency;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
public class CurrencyService {
    private final CurrencyRateClient client;
    private volatile Map<Currency, BigDecimal> lastSuccessfulRates = Map.of(Currency.UZS, BigDecimal.ONE);

    public CurrencyService(CurrencyRateClient client) {
        this.client = client;
    }

    @Cacheable(cacheNames = "currencyRates", key = "'UZS'", unless = "#result == null")
    @CircuitBreaker(name = "cbu", fallbackMethod = "ratesFallback")
    public Map<Currency, BigDecimal> rates() {
        Map<Currency, BigDecimal> rates = client.getRatesToUzs();
        if (!rates.containsKey(Currency.USD) || !rates.containsKey(Currency.EUR)) {
            throw new IllegalStateException("CBU response does not contain required rates");
        }
        Map<Currency, BigDecimal> copy = new EnumMap<>(Currency.class);
        copy.putAll(rates);
        lastSuccessfulRates = Map.copyOf(copy);
        return lastSuccessfulRates;
    }

    private Map<Currency, BigDecimal> ratesFallback(Throwable ignored) {
        return lastSuccessfulRates;
    }

    @CacheEvict(cacheNames = "currencyRates", key = "'UZS'")
    public void evictCache() {
    }

    public BigDecimal toUzs(BigDecimal amount, Currency currency) {
        return amount.multiply(rates().get(currency)).setScale(2, RoundingMode.HALF_UP);
    }
}
