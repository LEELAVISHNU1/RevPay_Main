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

/**
 * Implementation of LoanService.
 *
 * Purpose:
 * - Handles loan application
 * - Loan approval by admin
 * - EMI repayment (wallet & card)
 * - Loan status tracking
 */
@Service
public class LoanServiceImpl implements LoanService {

    // Logger for tracking loan-related activities
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

    // Fixed interest rate (12%)
    private final double INTEREST = 12.0;

    // Directory for storing uploaded loan documents
    private final String UPLOAD_DIR = "uploads/loans";

    @Value("${loan.upload.dir}")
    private String uploadDir;

    LoanServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    // ================= APPLY LOAN =================

    /**
     * Allows user to apply for a loan.
     *
     * Flow:
     * 1. Get current user.
     * 2. Save uploaded document.
     * 3. Calculate EMI.
     * 4. Create Loan entity with status PENDING.
     * 5. Save loan to database.
     * 6. Notify user.
     */
    @Override
    public void applyLoan(Double amount, Integer months, MultipartFile document) {

        User user = userService.getCurrentUser();

        logger.info("Loan application started | User: {} | Amount: {} | Months: {}",
                user.getEmail(), amount, months);

        try {
            // Ensure upload directory exists
            File folder = new File(UPLOAD_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
                logger.debug("Loan upload directory created at: {}", UPLOAD_DIR);
            }

            // Create unique file name
            String fileName = System.currentTimeMillis() + "_" + document.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            // Save file
            Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create loan entity
            Loan loan = new Loan();
            loan.setUser(user);
            loan.setPrincipalAmount(amount);
            loan.setInterestRate(INTEREST);
            loan.setTenureMonths(months);

            // Calculate EMI
            double emi = EmiCalculator.calculate(amount, INTEREST, months);
            loan.setEmiAmount(emi);

            // Total payable = EMI × months
            loan.setRemainingAmount(emi * months);

            loan.setStatus("PENDING");
            loan.setCreatedAt(LocalDateTime.now());

            // Save document metadata
            loan.setDocumentName(document.getOriginalFilename());
            loan.setDocumentType(document.getContentType());
            loan.setDocumentPath(fileName);

            loanRepository.save(loan);

            logger.info("Loan application saved successfully | Loan ID: {} | User: {}",
                    loan.getLoanId(), user.getEmail());

            // Notify user
            notificationService.notify(user,
                    "Loan Application Submitted",
                    "Your loan request for ₹" + amount + " is under review");

        } catch (IOException e) {
            logger.error("Loan document upload failed | User: {}", user.getEmail(), e);
            throw new RuntimeException("Document upload failed");
        }
    }

    // ================= APPROVE LOAN =================

    /**
     * Allows ADMIN to approve a loan.
     *
     * Flow:
     * 1. Verify current user is ADMIN.
     * 2. Fetch loan.
     * 3. Ensure loan is PENDING.
     * 4. Change status to APPROVED.
     * 5. Credit principal amount to user's wallet.
     * 6. Notify user.
     */
    @Override
    public void approveLoan(Long loanId) {

        User current = userService.getCurrentUser();

        logger.info("Loan approval attempt | Loan ID: {} | Admin: {}",
                loanId, current.getEmail());

        // Only ADMIN can approve
        if (!current.getRole().getRoleName().equals("ADMIN")) {
            logger.warn("Unauthorized loan approval attempt by user: {}", current.getEmail());
            throw new RuntimeException("Only admin can approve loans");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("Loan approval failed - Loan not found | ID: {}", loanId);
                    return new RuntimeException("Loan not found");
                });

        // Prevent re-approval
        if (!loan.getStatus().equals("PENDING")) {
            logger.warn("Loan approval rejected - Already processed | ID: {} | Status: {}",
                    loanId, loan.getStatus());
            throw new RuntimeException("Loan already processed");
        }

        loan.setStatus("APPROVED");
        loanRepository.save(loan);

        // Credit principal amount to user wallet
        walletService.creditUser(
                loan.getUser(),
                loan.getPrincipalAmount(),
                "Loan credited"
        );

        logger.info("Loan approved and credited | Loan ID: {} | Amount: {}",
                loanId, loan.getPrincipalAmount());

        notificationService.notify(loan.getUser(),
                "Loan Approved",
                "₹" + loan.getPrincipalAmount() + " credited to wallet");
    }

    // ================= REPAY EMI (WALLET) =================

    /**
     * Repays EMI using wallet balance.
     *
     * Flow:
     * 1. Fetch loan.
     * 2. Deduct EMI from wallet.
     * 3. Reduce remaining amount.
     * 4. If fully paid → mark CLOSED.
     * 5. Notify user.
     */
    @Override
    public void repayEmi(Long loanId) {

        logger.info("EMI repayment initiated (wallet) | Loan ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("EMI repayment failed - Loan not found | ID: {}", loanId);
                    return new RuntimeException("Loan not found");
                });

        // Deduct EMI from wallet
        walletService.debitUser(
                loan.getUser(),
                loan.getEmiAmount(),
                "Loan EMI Payment"
        );

        // Reduce remaining amount
        loan.setRemainingAmount(
                loan.getRemainingAmount() - loan.getEmiAmount()
        );

        // Close loan if fully paid
        if (loan.getRemainingAmount() <= 0) {
            loan.setRemainingAmount(0.0);
            loan.setStatus("CLOSED");
            logger.info("Loan fully closed | Loan ID: {}", loanId);
        }

        loanRepository.save(loan);

        notificationService.notify(loan.getUser(),
                "EMI Paid",
                "EMI ₹" + loan.getEmiAmount() + " deducted");

        logger.info("EMI payment successful | Loan ID: {} | Remaining: {}",
                loanId, loan.getRemainingAmount());
    }

    // ================= REPAY EMI (CARD) =================

    /**
     * Repays EMI using linked card.
     *
     * - Requires loan to be APPROVED.
     * - Deducts EMI via card.
     * - Updates remaining amount.
     */
    @Override
    @Transactional
    public void repayEmiUsingCard(Long loanId, Long cardId) {

        logger.info("EMI repayment initiated (card) | Loan ID: {} | Card ID: {}",
                loanId, cardId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("Card EMI payment failed - Loan not found | ID: {}", loanId);
                    return new RuntimeException("Loan not found");
                });

        if (!loan.getStatus().equals("APPROVED")) {
            logger.warn("Card EMI rejected - Loan not active | ID: {}", loanId);
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
            logger.info("Loan fully closed via card | Loan ID: {}", loanId);
        }

        loanRepository.save(loan);

        notificationService.notify(loan.getUser(),
                "EMI Paid (Card)",
                "₹" + loan.getEmiAmount() + " paid via card");

        logger.info("Card EMI payment successful | Loan ID: {} | Remaining: {}",
                loanId, loan.getRemainingAmount());
    }

    // ================= FETCH METHODS =================

    /**
     * Returns all loans of current user.
     */
    @Override
    public List<Loan> myLoans() {
        User user = userService.getCurrentUser();
        logger.info("Fetching loans for user: {}", user.getEmail());
        return loanRepository.findByUser(user);
    }

    /**
     * Returns all pending loans (admin view).
     */
    @Override
    public List<Loan> pendingLoans() {
        logger.info("Fetching all pending loans");
        return loanRepository.findByStatus("PENDING");
    }

    /**
     * Returns active (APPROVED) loans for current user.
     */
    @Override
    public List<Loan> myActiveLoans() {
        User user = userService.getCurrentUser();
        logger.info("Fetching active loans for user: {}", user.getEmail());
        return loanRepository.findByUserAndStatus(user, "APPROVED");
    }

    /**
     * Returns all loans (admin).
     */
    @Override
    public List<Loan> allLoans() {
        logger.info("Fetching all loans (admin view)");
        return loanRepository.findAll();
    }

    /**
     * Allows admin to reject a loan.
     *
     * - Only works if loan is PENDING.
     * - Changes status to REJECTED.
     */
    @Override
    public void rejectLoan(Long loanId) {

        logger.info("Loan rejection initiated | Loan ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("Loan rejection failed - Loan not found | ID: {}", loanId);
                    return new RuntimeException("Loan not found");
                });

        if (!loan.getStatus().equals("PENDING")) {
            logger.warn("Loan rejection rejected - Already processed | ID: {}", loanId);
            throw new RuntimeException("Loan already processed");
        }

        loan.setStatus("REJECTED");
        loanRepository.save(loan);

        logger.info("Loan rejected successfully | Loan ID: {}", loanId);
    }
}