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

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired 
    private WalletService walletService;

    @Autowired 
    private TransactionRepository transactionRepository;

    @Autowired 
    private InvoiceRepository invoiceRepository;

    @Autowired 
    private UserService userService;

    // Returns financial summary for the logged-in business user
    @Override
    public BusinessSummaryResponse getBusinessSummary() {

        User business = userService.getCurrentUser();

        Wallet wallet = walletService.getMyWallet();

        BusinessSummaryResponse res = new BusinessSummaryResponse();

        res.setWalletBalance(wallet.getBalance());

        res.setTotalReceived(
                transactionRepository.totalReceived(wallet)
        );

        res.setTotalSent(
                transactionRepository.totalSent(wallet)
        );

        res.setTotalInvoiceRevenue(
                invoiceRepository.totalRevenue(business)
        );

        return res;
    }
}