package com.revpay.controller.request;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.MoneyRequest;
import com.revpay.service.interfaces.RequestService;

@RestController
@RequestMapping("/api/requests")
@PreAuthorize("hasRole('PERSONAL')")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @PostMapping("/create")
    public ApiResponse<?> create(@RequestBody Map<String,Object> body) {
        requestService.createRequest(
                body.get("email").toString(),
                Double.valueOf(body.get("amount").toString()),
                body.get("note").toString()
        );
        return new ApiResponse<>(true, "Request sent", null);
    }

    @GetMapping("/incoming")
    public ApiResponse<?> incoming() {
        List<MoneyRequest> requests = requestService.myIncomingRequests();
        return new ApiResponse<>(true, "Incoming requests fetched", requests);
    }

    @PostMapping("/accept/{id}")
    public ApiResponse<?> accept(@PathVariable Long id) {
        requestService.acceptRequest(id);
        return new ApiResponse<>(true, "Request accepted", null);
    }

    @PostMapping("/decline/{id}")
    public ApiResponse<?> decline(@PathVariable Long id) {
        requestService.declineRequest(id);
        return new ApiResponse<>(true, "Request declined", null);
    }
}