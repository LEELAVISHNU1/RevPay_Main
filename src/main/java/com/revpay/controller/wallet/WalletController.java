package com.revpay.controller.wallet;

import com.revpay.dto.request.AddMoneyRequest;
import com.revpay.dto.request.CardDepositRequest;
import com.revpay.dto.request.SendMoneyRequest;
import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Wallet;
import com.revpay.service.interfaces.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("hasRole('PERSONAL')")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/balance")
    public ApiResponse<?> getBalance() {
        Wallet wallet = walletService.getMyWallet();

        return new ApiResponse<>(
                true,
                "Wallet balance fetched",
                Map.of(
                        "balance", wallet.getBalance(),
                        "status", wallet.getStatus()
                )
        );
    }

    @PostMapping("/add-money")
    public ApiResponse<?> addMoney(@RequestBody AddMoneyRequest request) {
        walletService.addMoney(request.getAmount());
        Wallet wallet = walletService.getMyWallet();

        return new ApiResponse<>(
                true,
                "Money added successfully",
                Map.of("newBalance", wallet.getBalance())
        );
    }

    @PostMapping("/send")
    public ApiResponse<?> sendMoney(@RequestBody SendMoneyRequest request) {
        walletService.sendMoney(
                request.getReceiverEmail(),
                request.getAmount(),
                request.getRemark()
        );
        return new ApiResponse<>(true, "Transfer successful", null);
    }

    @PostMapping("/deposit-card")
    public ApiResponse<?> depositViaCard(@RequestBody CardDepositRequest request) {
        walletService.addMoneyViaCard(
                request.getCardId(),
                request.getAmount()
        );
        return new ApiResponse<>(true, "Money added using card", null);
    }
}