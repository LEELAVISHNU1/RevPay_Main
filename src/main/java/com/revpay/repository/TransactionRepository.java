package com.revpay.repository;

import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
    
    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet = :wallet AND t.txnType='RECEIVE'")
    double totalReceived(Wallet wallet);

    @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet = :wallet AND t.txnType='SEND'")
    double totalSent(Wallet wallet);
}