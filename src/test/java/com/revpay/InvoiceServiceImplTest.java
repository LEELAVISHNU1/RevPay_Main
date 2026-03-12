package com.revpay;
import com.revpay.entity.Invoice;
import com.revpay.entity.User;
import com.revpay.repository.InvoiceRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.impl.InvoiceServiceImpl;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void testCreateInvoice_Success() {
       
        User business = new User();
        business.setUserId(1L);
        
        User customer = new User();
        customer.setUserId(2L);
        customer.setEmail("customer@test.com");
        
        when(userService.getCurrentUser()).thenReturn(business);
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        invoiceService.createInvoice("customer@test.com", 1000.00, "Test invoice", LocalDate.now().plusDays(7));

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    void testCreateInvoice_CustomerNotFound_ThrowsException() {

        User business = new User();
        business.setUserId(1L);
        
        when(userService.getCurrentUser()).thenReturn(business);
        when(userRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invoiceService.createInvoice("invalid@test.com", 1000.00, "Test", LocalDate.now().plusDays(7));
        });
        
        assertEquals("Customer not found", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }


    @Test
    void testPayInvoice_AlreadyProcessed_ThrowsException() {
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);
        invoice.setStatus("PAID");
        
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invoiceService.payInvoice(1L);
        });
        
        assertEquals("Invoice already processed", exception.getMessage());
        verify(walletService, never()).payToUser(any(), anyDouble(), anyString());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testPayInvoice_NotFound_ThrowsException() {

        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invoiceService.payInvoice(99L);
        });
        
        assertEquals("Invoice not found", exception.getMessage());
        verify(walletService, never()).payToUser(any(), anyDouble(), anyString());
    }

    @Test
    void testPayInvoiceUsingCard_Success() {
        // Given
        User business = new User();
        business.setUserId(1L);
        business.setEmail("business@test.com");
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(1L);
        invoice.setBusiness(business);
        invoice.setAmount(500.00);
        invoice.setStatus("PENDING");
        
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        doNothing().when(walletService).payUsingCard(10L, "business@test.com", 500.00, "Invoice #1");

        // When
        invoiceService.payInvoiceUsingCard(1L, 10L);

        // Then
        assertEquals("PAID", invoice.getStatus());
        verify(invoiceRepository, times(1)).save(invoice);
        verify(walletService, times(1)).payUsingCard(10L, "business@test.com", 500.00, "Invoice #1");
    }
}