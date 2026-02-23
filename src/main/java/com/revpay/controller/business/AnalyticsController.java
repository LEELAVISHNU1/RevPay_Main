package com.revpay.controller.business;

import com.revpay.dto.response.ApiResponse;
import com.revpay.dto.response.BusinessSummaryResponse;
import com.revpay.service.interfaces.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PreAuthorize("hasRole('BUSINESS')")   // ⭐ only BUSINESS
    @GetMapping("/summary")
    public ApiResponse<?> summary() {
        BusinessSummaryResponse response = analyticsService.getBusinessSummary();

        return new ApiResponse<>(
                true,
                "Business summary fetched successfully",
                response
        );
    }
}