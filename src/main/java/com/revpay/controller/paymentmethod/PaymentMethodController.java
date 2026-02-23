package com.revpay.controller.paymentmethod;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.PaymentMethod;
import com.revpay.service.interfaces.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addCard(@RequestBody Map<String, String> body) {

        paymentMethodService.addCard(
                body.get("number"),
                body.get("holder"),
                body.get("expiry"),
                body.get("cvv")
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Card added successfully", null)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> myCards() {

        List<PaymentMethod> cards = paymentMethodService.myCards();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Payment methods fetched successfully", cards)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

        paymentMethodService.deleteCard(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Card removed successfully", null)
        );
    }
}