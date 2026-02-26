package com.revpay.controller.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.revpay.dto.response.ApiResponse;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/me")
    public ApiResponse<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return new ApiResponse<>(
                true,
                "User fetched successfully",
                "Logged in as: " + auth.getName()
        );
    }
}