package com.revpay.controller.wallet;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revpay.dto.request.AddMoneyRequest;
import com.revpay.dto.request.SendMoneyRequest;
import com.revpay.entity.Wallet;
import com.revpay.service.interfaces.WalletService;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        Wallet wallet = walletService.getWalletOfCurrentUser();

        return ResponseEntity.ok(
                Map.of(
                        "balance", wallet.getBalance(),
                        "status", wallet.getWalletStatus()
                )
        );
    }

  
    @PostMapping("/add-money")
    public ResponseEntity<?> addMoney(@RequestBody AddMoneyRequest request) {

        walletService.addMoney(request.getAmount());

        Wallet wallet = walletService.getWalletOfCurrentUser();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Money added successfully",
                        "newBalance", wallet.getBalance()
                )
        );
    }

 
    @PostMapping("/send")
    public ResponseEntity<?> sendMoney(@RequestBody SendMoneyRequest request) {

        walletService.sendMoney(
                request.getReceiverEmail(),
                request.getAmount(),
                request.getRemark()
        );

        return ResponseEntity.ok(
                Map.of("message", "Transfer successful")
        );
    }
}