package com.revpay.service.impl;

import com.revpay.config.SecurityConfig;
import com.revpay.entity.Loan;
import com.revpay.entity.User;
import com.revpay.repository.LoanRepository;
import com.revpay.service.interfaces.LoanService;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import com.revpay.util.EmiCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;

@Service
public class LoanServiceImpl implements LoanService {

    private final SecurityConfig securityConfig;

    @Autowired private LoanRepository loanRepository;
    @Autowired private UserService userService;
    @Autowired private WalletService walletService;
    
    @Autowired
	private NotificationService notificationService;

    private final double INTEREST = 12.0; // fixed interest

    private final String UPLOAD_DIR = "uploads/loans";
    @Value("${loan.upload.dir}")
    private String uploadDir;

    LoanServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public void applyLoan(Double amount, Integer months, MultipartFile document) {

        User user = userService.getCurrentUser();

        try {

            // create folder if not exists
            File folder = new File(UPLOAD_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // generate unique file name
            String fileName = System.currentTimeMillis() + "_" + document.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Loan loan = new Loan();
            loan.setUser(user);
            loan.setPrincipalAmount(amount);
            loan.setInterestRate(INTEREST);
            loan.setTenureMonths(months);

            double emi = EmiCalculator.calculate(amount, INTEREST, months);
            loan.setEmiAmount(emi);
            loan.setRemainingAmount(emi * months);

            loan.setStatus("PENDING");
            loan.setCreatedAt(LocalDateTime.now());

            // 🔥 Save document info
            loan.setDocumentName(document.getOriginalFilename());
            loan.setDocumentType(document.getContentType());
            loan.setDocumentPath(fileName);

            loanRepository.save(loan);

            notificationService.notify(user,
                    "Loan Application Submitted",
                    "Your loan request for ₹" + amount + " is under review");

        } catch (IOException e) {
            throw new RuntimeException("Document upload failed");
        }
    }
    
    

    @Override
    public void approveLoan(Long loanId) {
    	
    	User current = userService.getCurrentUser();

    	if (!current.getRole().getRoleName().equals("ADMIN"))
    	    throw new RuntimeException("Only admin can approve loans");

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("PENDING"))
            throw new RuntimeException("Loan already processed");

        loan.setStatus("APPROVED");
        loanRepository.save(loan);

        // ⭐ credit borrower wallet
        walletService.creditUser(
                loan.getUser(),
                loan.getPrincipalAmount(),
                "Loan credited"
        );
        
        notificationService.notify(loan.getUser(),
                "Loan Approved",
                "₹" + loan.getPrincipalAmount() + " credited to wallet");
    }

    @Override
    public void repayEmi(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        walletService.debitUser(
                loan.getUser(),
                loan.getEmiAmount(),
                "Loan EMI Payment"
        ); // deduction simulation

        loan.setRemainingAmount(
                loan.getRemainingAmount() - loan.getEmiAmount()
        );

        if (loan.getRemainingAmount() <= 0) {
            loan.setRemainingAmount(0.0);
            loan.setStatus("CLOSED");
        }

        loanRepository.save(loan);
        
        notificationService.notify(loan.getUser(),
                "EMI Paid",
                "EMI ₹" + loan.getEmiAmount() + " deducted");
    }

    @Override
    public List<Loan> myLoans() {
        return loanRepository.findByUser(userService.getCurrentUser());
    }
    
    @Override
    public List<Loan> pendingLoans() {
        return loanRepository.findByStatus("PENDING");
    }
    
    @Override
    public void rejectLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("PENDING"))
            throw new RuntimeException("Loan already processed");

        loan.setStatus("REJECTED");
        loanRepository.save(loan);
    }
    
    @Override
    @Transactional
    public void repayEmiUsingCard(Long loanId, Long cardId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("APPROVED"))
            throw new RuntimeException("Loan not active");

        User user = userService.getCurrentUser();

        walletService.payLoanUsingCard(
                cardId,
                loan.getEmiAmount(),
                "Loan EMI Payment"
        );

        loan.setRemainingAmount(
                loan.getRemainingAmount() - loan.getEmiAmount()
        );

        if (loan.getRemainingAmount() <= 0) {
            loan.setStatus("CLOSED");
            loan.setRemainingAmount(0.0);
        }

        loanRepository.save(loan);

        notificationService.notify(user,
                "EMI Paid (Card)",
                "₹" + loan.getEmiAmount() + " paid via card");
    }
    
    @Override
    public List<Loan> myActiveLoans() {
        return loanRepository.findByUserAndStatus(
                userService.getCurrentUser(),
                "APPROVED"
        );
    }
    
    @Override
    public List<Loan> allLoans() {
        return loanRepository.findAll();
    }

}