package com.revpay.service.interfaces;

import com.revpay.entity.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InvoiceService {

    void createInvoice(String customerEmail, Double amount, String description, LocalDate dueDate);

    List<Invoice> myReceivedInvoices();

    void payInvoice(Long invoiceId);
    
    void payInvoiceUsingCard(Long invoiceId, Long cardId);
    
    Map<String, Object> getBusinessAnalytics();
    
    List<Invoice> myCreatedInvoices();
}
