	package com.revpay.service.impl;

import com.revpay.entity.Invoice;
import com.revpay.entity.User;
import com.revpay.repository.InvoiceRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.InvoiceService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of InvoiceService.
 *
 * Purpose:
 * - Handles invoice creation.
 * - Handles invoice payment (wallet & card).
 * - Provides invoice analytics for business users.
 */
@Service
public class InvoiceServiceImpl implements InvoiceService {

    // Logger to track invoice-related events
    private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    // ================= CREATE INVOICE =================

    /**
     * Creates a new invoice from a BUSINESS user to a CUSTOMER.
     *
     * Flow:
     * 1. Get current logged-in business user.
     * 2. Validate customer exists.
     * 3. Create invoice entity.
     * 4. Set status as PENDING.
     * 5. Save invoice to database.
     */
    @Override
    public void createInvoice(String customerEmail, Double amount,
                              String description, LocalDate dueDate) {

        // Get current business user
        User business = userService.getCurrentUser();

        logger.info("Invoice creation started | Business: {} | Customer: {} | Amount: {}",
                business.getEmail(), customerEmail, amount);

        // Fetch customer by email
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> {
                    logger.warn("Invoice creation failed - Customer not found: {}", customerEmail);
                    return new RuntimeException("Customer not found");
                });

        // Create invoice entity
        Invoice invoice = new Invoice();
        invoice.setBusiness(business);
        invoice.setCustomer(customer);
        invoice.setAmount(amount);
        invoice.setDescription(description);
        invoice.setStatus("PENDING");
        invoice.setDueDate(dueDate);
        invoice.setCreatedAt(LocalDateTime.now());

        // Save invoice
        invoiceRepository.save(invoice);

        logger.info("Invoice created successfully | Invoice ID: {} | Business: {}",
                invoice.getInvoiceId(), business.getEmail());
    }

    // ================= RECEIVED INVOICES =================

    /**
     * Returns all PENDING invoices received by current user.
     */
    @Override
    public List<Invoice> myReceivedInvoices() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching received invoices for user: {}", currentUser.getEmail());

        return invoiceRepository.findByCustomerAndStatus(currentUser, "PENDING");
    }

    // ================= PAY INVOICE (WALLET) =================

    /**
     * Pays invoice using wallet balance.
     *
     * Flow:
     * 1. Fetch invoice.
     * 2. Ensure invoice is still PENDING.
     * 3. Transfer money to business wallet.
     * 4. Mark invoice as PAID.
     *
     * @Transactional ensures:
     * - If anything fails → rollback changes.
     */
    @Override
    @Transactional
    public void payInvoice(Long invoiceId) {

        logger.info("Invoice payment initiated (wallet) | Invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    logger.warn("Invoice payment failed - Invoice not found | ID: {}", invoiceId);
                    return new RuntimeException("Invoice not found");
                });

        // Prevent double payment
        if (!invoice.getStatus().equals("PENDING")) {
            logger.warn("Invoice payment rejected - Already processed | ID: {} | Status: {}",
                    invoiceId, invoice.getStatus());
            throw new RuntimeException("Invoice already processed");
        }

        // Transfer money from wallet
        walletService.payToUser(
                invoice.getBusiness(),
                invoice.getAmount(),
                "Invoice Payment #" + invoiceId
        );

        // Mark invoice as paid
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        logger.info("Invoice paid successfully (wallet) | Invoice ID: {}", invoiceId);
    }

    // ================= PAY INVOICE (CARD) =================

    /**
     * Pays invoice using linked card.
     *
     * Flow:
     * 1. Fetch invoice.
     * 2. Validate invoice status.
     * 3. Use card payment method.
     * 4. Mark invoice as PAID.
     */
    @Override
    @Transactional
    public void payInvoiceUsingCard(Long invoiceId, Long cardId) {

        logger.info("Invoice payment initiated (card) | Invoice ID: {} | Card ID: {}",
                invoiceId, cardId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    logger.warn("Card payment failed - Invoice not found | ID: {}", invoiceId);
                    return new RuntimeException("Invoice not found");
                });

        if (!invoice.getStatus().equals("PENDING")) {
            logger.warn("Card payment rejected - Already processed | ID: {} | Status: {}",
                    invoiceId, invoice.getStatus());
            throw new RuntimeException("Already processed");
        }

        // Process payment via card
        walletService.payUsingCard(
                cardId,
                invoice.getBusiness().getEmail(),
                invoice.getAmount(),
                "Invoice #" + invoiceId
        );

        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        logger.info("Invoice paid successfully (card) | Invoice ID: {}", invoiceId);
    }

    // ================= ANALYTICS =================

    /**
     * Returns business analytics summary.
     *
     * Includes:
     * - Total invoices created
     * - Pending invoices
     * - Total revenue earned
     * - Total customers served
     */
    @Override
    public Map<String, Object> getBusinessAnalytics() {

        User business = userService.getCurrentUser();

        logger.info("Fetching business analytics for: {}", business.getEmail());

        Map<String, Object> analytics = new HashMap<>();

        analytics.put("totalInvoices",
                invoiceRepository.countByBusiness(business));

        analytics.put("pendingInvoices",
                invoiceRepository.countByBusinessAndStatus(business, "PENDING"));

        analytics.put("totalRevenue",
                invoiceRepository.totalRevenue(business));

        analytics.put("totalCustomers",
                invoiceRepository.totalCustomers(business));

        logger.info("Business analytics fetched successfully for: {}",
                business.getEmail());

        return analytics;
    }

    // ================= CREATED INVOICES =================

    /**
     * Returns all invoices created by current business user.
     */
    @Override
    public List<Invoice> myCreatedInvoices() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching created invoices for business: {}",
                currentUser.getEmail());

        return invoiceRepository.findByBusiness(currentUser);
    }
}