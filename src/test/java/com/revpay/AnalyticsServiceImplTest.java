package com.revpay;

import com.revpay.dto.response.BusinessSummaryResponse;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.InvoiceRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.service.impl.AnalyticsServiceImpl;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    public void testGetBusinessSummary_Success() {

        User user = new User();
        user.setUserId(1L);
        
        Wallet wallet = new Wallet();
        wallet.setBalance(5000.00);
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(walletService.getMyWallet()).thenReturn(wallet);
        when(transactionRepository.totalReceived(wallet)).thenReturn(10000.00);
        when(transactionRepository.totalSent(wallet)).thenReturn(3000.00);
        when(invoiceRepository.totalRevenue(user)).thenReturn(7500.00);

        BusinessSummaryResponse response = analyticsService.getBusinessSummary();

        assertNotNull(response);
        assertEquals(5000.00, response.getWalletBalance());
        assertEquals(10000.00, response.getTotalReceived());
        assertEquals(3000.00, response.getTotalSent());
        assertEquals(7500.00, response.getTotalInvoiceRevenue());
        
        verify(userService, times(1)).getCurrentUser();
        verify(walletService, times(1)).getMyWallet();
        verify(transactionRepository, times(1)).totalReceived(wallet);
        verify(transactionRepository, times(1)).totalSent(wallet);
        verify(invoiceRepository, times(1)).totalRevenue(user);
    }

    @Test
    public void testGetBusinessSummary_WhenNoTransactions_ReturnsZero() {

        User user = new User();
        user.setUserId(1L);
        
        Wallet wallet = new Wallet();
        wallet.setBalance(5000.00);
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(walletService.getMyWallet()).thenReturn(wallet);
        when(transactionRepository.totalReceived(wallet)).thenReturn(0.0);
        when(transactionRepository.totalSent(wallet)).thenReturn(0.0);
        when(invoiceRepository.totalRevenue(user)).thenReturn(0.0);

        BusinessSummaryResponse response = analyticsService.getBusinessSummary();

        assertNotNull(response);
        assertEquals(5000.00, response.getWalletBalance());
        assertEquals(0.0, response.getTotalReceived());
        assertEquals(0.0, response.getTotalSent());
        assertEquals(0.0, response.getTotalInvoiceRevenue());
    }
}