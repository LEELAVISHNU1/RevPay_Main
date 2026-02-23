package com.revpay.controller.business;

import com.revpay.dto.response.ApiResponse;
import com.revpay.dto.response.BusinessSummaryResponse;
import com.revpay.service.interfaces.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<BusinessSummaryResponse>> summary() {

        BusinessSummaryResponse summary =
                analyticsService.getBusinessSummary();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Business summary fetched successfully",
                        summary
                )
        );
    }
}