package com.examplesubscribemaster.controller;

import com.examplesubscribemaster.dto.statistics.*;
import com.examplesubscribemaster.security.UserPrincipal;
import com.examplesubscribemaster.service.StatisticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService s;

    public StatisticsController(StatisticsService s) {
        this.s = s;
    }

    @GetMapping
    public StatisticsResponse stats(Authentication a) {
        return s.get(((UserPrincipal) a.getPrincipal()).id());
    }

    @GetMapping("/most-expensive")
    public Object most(Authentication a) {
        return s.get(((UserPrincipal) a.getPrincipal()).id()).mostExpensive();
    }

    @GetMapping("/monthly-expense")
    public Object monthly(Authentication a) {
        return s.get(((UserPrincipal) a.getPrincipal()).id()).currentMonthBaseCurrencyTotal();
    }

    @GetMapping("/monthly-trend")
    public Object trend(Authentication a, @RequestParam(defaultValue = "12") int months) {
        return s.get(((UserPrincipal) a.getPrincipal()).id(), months).monthlyTrend();
    }
}
