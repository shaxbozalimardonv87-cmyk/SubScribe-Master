package com.examplesubscribemaster.client;

import com.examplesubscribemaster.enums.Currency;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CbuCurrencyRateClient implements CurrencyRateClient {
    private final RestClient client;

    public CbuCurrencyRateClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.client = builder.requestFactory(factory).baseUrl("https://cbu.uz").build();
    }

    @Override
    public Map<Currency, BigDecimal> getRatesToUzs() {
        var result = new EnumMap<Currency, BigDecimal>(Currency.class);
        result.put(Currency.UZS, BigDecimal.ONE);
        List<?> rows = client.get().uri("/uz/arkhiv-kursov-valyut/json/").retrieve().body(List.class);
        if (rows != null) {
            for (Object row : rows) {
                if (row instanceof Map<?, ?> map) {
                    String code = String.valueOf(map.get("Ccy"));
                    BigDecimal rate = new BigDecimal(String.valueOf(map.get("Rate")));
                    if ("USD".equals(code)) result.put(Currency.USD, rate);
                    if ("EUR".equals(code)) result.put(Currency.EUR, rate);
                }
            }
        }
        return result;
    }
}
