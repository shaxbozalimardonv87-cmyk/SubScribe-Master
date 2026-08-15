package com.examplesubscribemaster.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        var m = new CaffeineCacheManager("currencyRates");
        m.setCaffeine(Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).maximumSize(10));
        return m;
    }
}
