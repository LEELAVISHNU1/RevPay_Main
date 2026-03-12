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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;

@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LogManager.getLogger(LoanServiceImpl.class);

    private final SecurityConfig securityConfig;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private NotificationService notificationService;

    private final double INTEREST = 12.0;

    private final String UPLOAD_DIR = "uploads/loans";

    @Value("${loan.upload.dir}")
    private String uploadDir;

    LoanServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    // Applies for a loan by saving document, calculating EMI, and storing loan request
    @Override
    public void applyLoan(Double amount, Integer months, MultipartFile document) {

        User user = userService.getCurrentUser();

        logger.info("Loan application started | User: {} | Amount: {} | Months: {}",
                user.getEmail(), amount, months);

        try {

            File folder = new File(UPLOAD_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
                logger.debug("Loan upload directory created at: {}", UPLOAD_DIR);
            }

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

            loan.setDocumentName(document.getOriginalFilename());
            loan.setDocumentType(document.getContentType());
            loan.setDocumentPath(fileName);

            loanRepository.save(loan);

            notificationService.notify(user,
                    "Loan Application Submitted",
                    "Your loan request for ₹" + amount + " is under review");

        } catch (IOException e) {
            logger.error("Loan document upload failed | User: {}", user.getEmail(), e);
            throw new RuntimeException("Document upload failed");
        }
    }

    // Approves loan by admin and credits loan amount to user's wallet
    @Override
    public void approveLoan(Long loanId) {

        User current = userService.getCurrentUser();

        if (!current.getRole().getRoleName().equals("ADMIN")) {
            throw new RuntimeException("Only admin can approve loans");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("PENDING")) {
            throw new RuntimeException("Loan already processed");
        }

        loan.setStatus("APPROVED");
        loanRepository.save(loan);

        walletService.creditUser(
                loan.getUser(),
                loan.getPrincipalAmount(),
                "Loan credited"
        );

        notificationService.notify(loan.getUser(),
                "Loan Approved",
                "₹" + loan.getPrincipalAmount() + " credited to wallet");
    }

    // Repays EMI using wallet and updates remaining loan balance
    @Override
    public void repayEmi(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        walletService.debitUser(
                loan.getUser(),
                loan.getEmiAmount(),
                "Loan EMI Payment"
        );

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

    // Repays EMI using linked card and updates remaining balance
    @Override
    @Transactional
    public void repayEmiUsingCard(Long loanId, Long cardId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("APPROVED")) {
            throw new RuntimeException("Loan not active");
        }

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

        notificationService.notify(loan.getUser(),
                "EMI Paid (Card)",
                "₹" + loan.getEmiAmount() + " paid via card");
    }

    // Returns all loans of the current user
    @Override
    public List<Loan> myLoans() {
        User user = userService.getCurrentUser();
        return loanRepository.findByUser(user);
    }

    // Returns all pending loan applications for admin
    @Override
    public List<Loan> pendingLoans() {
        return loanRepository.findByStatus("PENDING");
    }

    // Returns active loans of the current user
    @Override
    public List<Loan> myActiveLoans() {
        User user = userService.getCurrentUser();
        return loanRepository.findByUserAndStatus(user, "APPROVED");
    }

    // Returns all loans (admin view)
    @Override
    public List<Loan> allLoans() {
        return loanRepository.findAll();
    }

    // Rejects a pending loan and notifies the user
    @Override
    public void rejectLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getStatus().equals("PENDING")) {
            throw new RuntimeException("Loan already processed");
        }

        loan.setStatus("REJECTED");
        loanRepository.save(loan);

        notificationService.notify(loan.getUser(),
                "Loan Rejected",
                "Your loan application for ₹" + loan.getPrincipalAmount() + " has been rejected");
    }
}