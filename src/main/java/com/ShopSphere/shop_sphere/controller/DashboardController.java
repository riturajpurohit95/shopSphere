package com.ShopSphere.shop_sphere.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto;
import com.ShopSphere.shop_sphere.service.DashboardSummaryService;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")

public class DashboardController {

    private final DashboardSummaryService dashboardSummaryService;

    public DashboardController(DashboardSummaryService dashboardSummaryService) {
        this.dashboardSummaryService = dashboardSummaryService;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryDto getDashboardSummary() {
        return dashboardSummaryService.getSummary();
    }
}
