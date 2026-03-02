package com.revpay.repository;

import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);

	@Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet = :wallet AND t.txnType='RECEIVE'")
	double totalReceived(Wallet wallet);

	@Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.wallet = :wallet AND t.txnType='SEND'")
	double totalSent(Wallet wallet);

	Page<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet, Pageable pageable);

	@Query("""
			SELECT t FROM Transaction t
			WHERE t.wallet = :wallet
			AND (:type IS NULL OR t.txnType = :type)
			AND (:fromDate IS NULL OR t.createdAt >= :fromDate)
			AND (:toDate IS NULL OR t.createdAt <= :toDate)
			""")
	Page<Transaction> searchTransactions(@Param("wallet") Wallet wallet, @Param("type") String type,
			@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, Pageable pageable);
}
