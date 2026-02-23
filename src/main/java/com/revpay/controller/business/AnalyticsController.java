package com.revpay.controller.business;

import com.revpay.dto.response.BusinessSummaryResponse;
import com.revpay.service.interfaces.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public BusinessSummaryResponse summary() {
        return analyticsService.getBusinessSummary();
    }
}