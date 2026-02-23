package com.revpay.controller.auth;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.service.interfaces.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> history() {

        Wallet wallet = walletService.getWalletOfCurrentUser();

        List<Transaction> transactions =
                transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Transactions fetched successfully", transactions)
        );
    }
}