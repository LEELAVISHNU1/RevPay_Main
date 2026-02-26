package com.revpay.service.interfaces;

import com.revpay.entity.Loan;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface LoanService {

	void applyLoan(Double amount, Integer months, MultipartFile document);

    void approveLoan(Long loanId);

    void repayEmi(Long loanId);

    List<Loan> myLoans();
    
    List<Loan> pendingLoans();
    
    void rejectLoan(Long loanId);
    
    void repayEmiUsingCard(Long loanId, Long cardId);
    
    List<Loan> myActiveLoans();
    
    List<Loan> allLoans();
}