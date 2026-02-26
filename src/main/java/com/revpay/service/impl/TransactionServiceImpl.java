package com.revpay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.revpay.dto.response.PageResponse;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.WalletRepository;
import com.revpay.service.interfaces.TransactionService;
import com.revpay.service.interfaces.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    // ================= MY TRANSACTIONS =================
    @Override
    public PageResponse<?> myTransactions(int page, int size) {

        User currentUser = userService.getCurrentUser();

        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactionPage =
                transactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable);

        return new PageResponse<>(
                transactionPage.getContent(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getTotalElements()
        );
    }

    // ================= SEARCH TRANSACTIONS =================
    @Override
    public PageResponse<?> searchTransactions(
            int page,
            int size,
            String type,
            String from,
            String to,
            String sort) {

        User currentUser = userService.getCurrentUser();

        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Sorting
        Sort sorting = Sort.by("createdAt").descending();
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sorting = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        // Date parsing
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (from != null && !from.isEmpty())
            fromDate = LocalDate.parse(from).atStartOfDay();

        if (to != null && !to.isEmpty())
            toDate = LocalDate.parse(to).atTime(23, 59, 59);

        Page<Transaction> result =
                transactionRepository.searchTransactions(
                        wallet, type, fromDate, toDate, pageable
                );

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
        );
    }

    // ================= CREATE TRANSACTION =================
    @Override
    public void createTransaction(User user, Double amount, String description) {

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Transaction t = new Transaction();

        t.setWallet(wallet);
        t.setAmount(amount);

        // Auto set type
        if (amount < 0) {
            t.setTxnType("SEND");
        } else {
            t.setTxnType("RECEIVE");
        }

        t.setBalanceAfterTxn(wallet.getBalance());
        t.setRemark(description);
        t.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(t);
    }
}