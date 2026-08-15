package com.examplesubscribemaster.controller;

import com.examplesubscribemaster.service.AdminStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminStatisticsService service;

    public AdminController(AdminStatisticsService service) {
        this.service = service;
    }

    @GetMapping("/statistics")
    public AdminStatisticsService.AdminOverview statistics() {
        return service.getOverview();
    }
}
