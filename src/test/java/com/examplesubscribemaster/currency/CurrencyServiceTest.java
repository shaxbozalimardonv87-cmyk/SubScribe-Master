package com.examplesubscribemaster.currency;

import com.examplesubscribemaster.client.CurrencyRateClient;
import com.examplesubscribemaster.enums.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock private CurrencyRateClient client;

    private CurrencyService service;

    @BeforeEach
    void setUp() {
        service = new CurrencyService(client);
    }

    private Map<Currency, BigDecimal> validRates() {
        Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
        rates.put(Currency.USD, new BigDecimal("12650.50"));
        rates.put(Currency.EUR, new BigDecimal("13700.25"));
        rates.put(Currency.UZS, BigDecimal.ONE);
        return rates;
    }

    @Test
    void rates_validClientResponse_returnsAndCachesRates() {
        when(client.getRatesToUzs()).thenReturn(validRates());

        Map<Currency, BigDecimal> rates = service.rates();

        assertThat(rates.get(Currency.USD)).isEqualByComparingTo("12650.50");
        assertThat(rates.get(Currency.EUR)).isEqualByComparingTo("13700.25");
    }

    @Test
    void rates_missingRequiredCurrency_throwsIllegalState() {
        when(client.getRatesToUzs()).thenReturn(Map.of(Currency.UZS, BigDecimal.ONE));

        assertThatThrownBy(() -> service.rates()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void toUzs_multipliesAmountByRateAndScalesToTwoDecimals() {
        when(client.getRatesToUzs()).thenReturn(validRates());

        BigDecimal result = service.toUzs(new BigDecimal("2"), Currency.USD);

        assertThat(result).isEqualByComparingTo(new BigDecimal("25301.00"));
    }

    @Test
    void toUzs_uzsAmount_isUnchanged() {
        when(client.getRatesToUzs()).thenReturn(validRates());

        BigDecimal result = service.toUzs(new BigDecimal("1000"), Currency.UZS);

        assertThat(result).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void ratesFallback_afterSuccessfulCall_returnsLastSuccessfulRates() throws Exception {
        when(client.getRatesToUzs()).thenReturn(validRates());
        Map<Currency, BigDecimal> fetched = service.rates();

        Method fallback = CurrencyService.class.getDeclaredMethod("ratesFallback", Throwable.class);
        fallback.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Currency, BigDecimal> fallbackResult = (Map<Currency, BigDecimal>) fallback.invoke(service, new RuntimeException("cbu.uz timeout"));

        assertThat(fallbackResult).isEqualTo(fetched);
    }

    @Test
    void ratesFallback_beforeAnySuccessfulCall_returnsDefaultUzsOnlyRates() throws Exception {
        Method fallback = CurrencyService.class.getDeclaredMethod("ratesFallback", Throwable.class);
        fallback.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Currency, BigDecimal> fallbackResult = (Map<Currency, BigDecimal>) fallback.invoke(service, new RuntimeException("cbu.uz down"));

        assertThat(fallbackResult).containsEntry(Currency.UZS, BigDecimal.ONE);
    }
}
