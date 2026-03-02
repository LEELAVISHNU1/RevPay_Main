package com.revpay.repository;

import com.revpay.entity.Loan;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	List<Loan> findByUser(User user);

	List<Loan> findByStatus(String status);

	List<Loan> findByUserAndStatus(User user, String status);
}