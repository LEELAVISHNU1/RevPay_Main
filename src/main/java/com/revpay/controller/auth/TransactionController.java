package com.revpay.controller.auth;

import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.service.interfaces.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Transaction> history() {

     
        Wallet wallet = walletService.getWalletOfCurrentUser();

        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet);
    }
}