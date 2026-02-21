package com.revpay.repository;

import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
}