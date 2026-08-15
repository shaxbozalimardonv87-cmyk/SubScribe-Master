package com.examplesubscribemaster.service;

import com.examplesubscribemaster.dto.statistics.*;

public interface StatisticsService {
    StatisticsResponse get(Long userId, int months);

    default StatisticsResponse get(Long userId) {
        return get(userId, 12);
    }
}
