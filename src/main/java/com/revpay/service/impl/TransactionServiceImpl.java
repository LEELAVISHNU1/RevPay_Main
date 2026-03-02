package com.revpay.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Service implementation for handling user transactions.
 *
 * Responsibilities:
 * - Fetch paginated transactions
 * - Search transactions with filters
 * - Create new transaction record
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    // Logger for tracking transaction-related activities
    private static final Logger logger =
            LogManager.getLogger(TransactionServiceImpl.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    // ================= MY TRANSACTIONS =================

    /**
     * Returns paginated transactions of currently logged-in user.
     *
     * Flow:
     * 1. Get current user
     * 2. Fetch user's wallet
     * 3. Fetch transactions sorted by latest first
     * 4. Return PageResponse with content + pagination info
     */
    @Override
    public PageResponse<?> myTransactions(int page, int size) {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching transactions for user: {} | page={} size={}",
                currentUser.getEmail(), page, size);

        // Fetch wallet of logged-in user
        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    logger.error("Wallet not found for user: {}", currentUser.getEmail());
                    return new RuntimeException("Wallet not found");
                });

        // Create pagination object
        Pageable pageable = PageRequest.of(page, size);

        // Fetch transactions sorted by createdAt descending
        Page<Transaction> transactionPage =
                transactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable);

        // Return wrapped response
        return new PageResponse<>(
                transactionPage.getContent(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getTotalElements()
        );
    }

    // ================= SEARCH TRANSACTIONS =================

    /**
     * Searches transactions using optional filters:
     * - Transaction type (SEND / RECEIVE)
     * - From date
     * - To date
     * - Sorting
     *
     * Supports pagination.
     */
    @Override
    public PageResponse<?> searchTransactions(
            int page,
            int size,
            String type,
            String from,
            String to,
            String sort) {

        User currentUser = userService.getCurrentUser();

        logger.info("Searching transactions for user: {}", currentUser.getEmail());

        // Fetch wallet
        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    logger.error("Wallet not found for user: {}", currentUser.getEmail());
                    return new RuntimeException("Wallet not found");
                });

        // Default sorting: latest first
        Sort sorting = Sort.by("createdAt").descending();

        // If custom sorting is provided (format: field,direction)
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sorting = Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
        }

        Pageable pageable = PageRequest.of(page, size, sorting);

        // Convert date strings to LocalDateTime
        LocalDateTime fromDate = null;
        LocalDateTime toDate = null;

        if (from != null && !from.isEmpty())
            fromDate = LocalDate.parse(from).atStartOfDay();

        if (to != null && !to.isEmpty())
            toDate = LocalDate.parse(to).atTime(23, 59, 59);

        // Perform filtered search
        Page<Transaction> result =
                transactionRepository.searchTransactions(
                        wallet, type, fromDate, toDate, pageable
                );

        logger.info("Transaction search completed. Results count: {}",
                result.getTotalElements());

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements()
        );
    }

    // ================= CREATE TRANSACTION =================

    /**
     * Creates a new transaction record for a user.
     *
     * Used internally when:
     * - Sending money
     * - Receiving money
     * - Paying invoice
     * - Loan credit
     * - EMI payment
     *
     * Logic:
     * - If amount < 0 → SEND transaction
     * - If amount > 0 → RECEIVE transaction
     */
    @Override
    public void createTransaction(User user, Double amount, String description) {

        logger.info("Creating transaction for user: {} | amount={}",
                user.getEmail(), amount);

        // Fetch wallet
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("Wallet not found for user: {}", user.getEmail());
                    return new RuntimeException("Wallet not found");
                });

        // Create new transaction
        Transaction t = new Transaction();
        t.setWallet(wallet);
        t.setAmount(amount);

        // Determine transaction type
        if (amount < 0) {
            t.setTxnType("SEND");
        } else {
            t.setTxnType("RECEIVE");
        }

        // Store balance after transaction
        t.setBalanceAfterTxn(wallet.getBalance());

        t.setRemark(description);
        t.setCreatedAt(LocalDateTime.now());

        // Save transaction
        transactionRepository.save(t);

        logger.info("Transaction saved successfully for user: {}", user.getEmail());
    }
}