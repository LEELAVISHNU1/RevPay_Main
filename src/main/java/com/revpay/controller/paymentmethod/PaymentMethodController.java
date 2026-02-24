package com.revpay.controller.paymentmethod;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.PaymentMethod;
import com.revpay.service.interfaces.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@PreAuthorize("isAuthenticated()")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PostMapping("/add")
    public ApiResponse<?> addCard(@RequestBody Map<String,String> body) {
        paymentMethodService.addCard(
                body.get("number"),
                body.get("holder"),
                body.get("expiry"),
                body.get("cvv")
        );
        return new ApiResponse<>(true, "Card added successfully", null);
    }

    @GetMapping
    public ApiResponse<?> myCards() {
        List<PaymentMethod> cards = paymentMethodService.myCards();
        return new ApiResponse<>(true, "Cards fetched successfully", cards);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        paymentMethodService.deleteCard(id);
        return new ApiResponse<>(true, "Card removed", null);
    }
}