package com.examplesubscribemaster.scheduler;

import com.examplesubscribemaster.currency.CurrencyService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyRateRefreshScheduler {
    private final CurrencyService currencyService;

    public CurrencyRateRefreshScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "currencyRateRefreshScheduler", lockAtMostFor = "PT10M", lockAtLeastFor = "PT1M")
    public void refresh() {
        currencyService.evictCache();
        currencyService.rates();
    }
}
