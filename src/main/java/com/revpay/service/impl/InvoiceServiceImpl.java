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

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger logger = LogManager.getLogger(InvoiceServiceImpl.class);

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    // Creates a new invoice from business user to customer
    @Override
    public void createInvoice(String customerEmail, Double amount,
                              String description, LocalDate dueDate) {

        User business = userService.getCurrentUser();

        logger.info("Invoice creation started | Business: {} | Customer: {} | Amount: {}",
                business.getEmail(), customerEmail, amount);

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> {
                    logger.warn("Invoice creation failed - Customer not found: {}", customerEmail);
                    return new RuntimeException("Customer not found");
                });

        Invoice invoice = new Invoice();
        invoice.setBusiness(business);
        invoice.setCustomer(customer);
        invoice.setAmount(amount);
        invoice.setDescription(description);
        invoice.setStatus("PENDING");
        invoice.setDueDate(dueDate);
        invoice.setCreatedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);

        logger.info("Invoice created successfully | Invoice ID: {} | Business: {}",
                invoice.getInvoiceId(), business.getEmail());
    }

    // Returns pending invoices received by current user
    @Override
    public List<Invoice> myReceivedInvoices() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching received invoices for user: {}", currentUser.getEmail());

        return invoiceRepository.findByCustomerAndStatus(currentUser, "PENDING");
    }

    // Pays invoice using wallet balance
    @Override
    @Transactional
    public void payInvoice(Long invoiceId) {

        logger.info("Invoice payment initiated (wallet) | Invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    logger.warn("Invoice payment failed - Invoice not found | ID: {}", invoiceId);
                    return new RuntimeException("Invoice not found");
                });

        if (!invoice.getStatus().equals("PENDING")) {
            logger.warn("Invoice payment rejected - Already processed | ID: {} | Status: {}",
                    invoiceId, invoice.getStatus());
            throw new RuntimeException("Invoice already processed");
        }

        walletService.payToUser(
                invoice.getBusiness(),
                invoice.getAmount(),
                "Invoice Payment #" + invoiceId
        );

        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);

        logger.info("Invoice paid successfully (wallet) | Invoice ID: {}", invoiceId);
    }

    // Pays invoice using a linked card
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

    // Returns analytics data for business dashboard
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

    // Returns all invoices created by the business user
    @Override
    public List<Invoice> myCreatedInvoices() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching created invoices for business: {}",
                currentUser.getEmail());

        return invoiceRepository.findByBusiness(currentUser);
    }
}