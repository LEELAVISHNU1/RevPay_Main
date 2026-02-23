package com.revpay.controller.wallet;

import com.revpay.dto.request.AddMoneyRequest;
import com.revpay.dto.request.CardDepositRequest;
import com.revpay.dto.request.SendMoneyRequest;
import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Wallet;
import com.revpay.service.interfaces.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    // ✅ Get Wallet Balance
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance() {

        Wallet wallet = walletService.getWalletOfCurrentUser();

        Map<String, Object> data = Map.of(
                "balance", wallet.getBalance(),
                "status", wallet.getWalletStatus()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Wallet fetched successfully", data)
        );
    }

    // ✅ Add Money (Manual)
    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMoney(
            @RequestBody AddMoneyRequest request) {

        walletService.addMoney(request.getAmount());

        Wallet wallet = walletService.getWalletOfCurrentUser();

        Map<String, Object> data = Map.of(
                "newBalance", wallet.getBalance()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Money added successfully", data)
        );
    }

    // ✅ Send Money
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendMoney(
            @RequestBody SendMoneyRequest request) {

        walletService.sendMoney(
                request.getReceiverEmail(),
                request.getAmount(),
                request.getRemark()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transfer successful", null)
        );
    }

    // ✅ Deposit via Card
    @PostMapping("/deposit-card")
    public ResponseEntity<ApiResponse<Void>> depositViaCard(
            @RequestBody CardDepositRequest request) {

        walletService.addMoneyViaCard(
                request.getCardId(),
                request.getAmount()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Money added using card", null)
        );
    }
}