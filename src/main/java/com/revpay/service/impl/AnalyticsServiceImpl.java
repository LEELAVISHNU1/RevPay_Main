package com.revpay.service.impl;

import com.revpay.dto.response.BusinessSummaryResponse;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.InvoiceRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.service.interfaces.AnalyticsService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of AnalyticsService.
 *
 * Purpose:
 * - Provides business-related financial analytics.
 * - Used mainly for BUSINESS users dashboard.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    // Used to fetch current user's wallet
    @Autowired 
    private WalletService walletService;

    // Used to calculate total sent and received transactions
    @Autowired 
    private TransactionRepository transactionRepository;

    // Used to calculate invoice revenue
    @Autowired 
    private InvoiceRepository invoiceRepository;

    // Used to get currently logged-in user
    @Autowired 
    private UserService userService;

    /**
     * Returns summary analytics for a BUSINESS user.
     *
     * Function:
     * - Fetch current logged-in business user.
     * - Fetch their wallet details.
     * - Calculate:
     *      1. Current wallet balance
     *      2. Total money received
     *      3. Total money sent
     *      4. Total revenue from invoices
     * - Wraps all values inside BusinessSummaryResponse.
     *
     * @return BusinessSummaryResponse containing financial summary
     */
    @Override
    public BusinessSummaryResponse getBusinessSummary() {

        // Get logged-in business user
        User business = userService.getCurrentUser();

        // Get wallet of current user
        Wallet wallet = walletService.getMyWallet();

        // Create response object
        BusinessSummaryResponse res = new BusinessSummaryResponse();

        // Set current wallet balance
        res.setWalletBalance(wallet.getBalance());

        // Set total money received into wallet
        res.setTotalReceived(
                transactionRepository.totalReceived(wallet)
        );

        // Set total money sent from wallet
        res.setTotalSent(
                transactionRepository.totalSent(wallet)
        );

        // Set total revenue earned via invoices
        res.setTotalInvoiceRevenue(
                invoiceRepository.totalRevenue(business)
        );

        return res;
    }
}