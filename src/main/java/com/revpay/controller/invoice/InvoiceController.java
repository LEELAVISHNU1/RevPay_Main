package com.revpay.controller.invoice;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Invoice;
import com.revpay.service.interfaces.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // BUSINESS creates invoices
    @PreAuthorize("hasRole('BUSINESS')")
    @PostMapping("/create")
    public ApiResponse<?> create(@RequestBody Map<String, String> body) {

        invoiceService.createInvoice(
                body.get("customerEmail"),
                Double.valueOf(body.get("amount")),
                body.get("description"),
                LocalDate.parse(body.get("dueDate"))
        );

        return new ApiResponse<>(true, "Invoice created", null);
    }

    // PERSONAL views received invoices
    @PreAuthorize("hasRole('PERSONAL')")
    @GetMapping("/received")
    public ApiResponse<?> received() {
        List<Invoice> invoices = invoiceService.myReceivedInvoices();

        return new ApiResponse<>(
                true,
                "Received invoices fetched successfully",
                invoices
        );
    }

    // PERSONAL pays invoice
    @PreAuthorize("hasRole('PERSONAL')")
    @PostMapping("/pay/{id}")
    public ApiResponse<?> pay(@PathVariable Long id) {
        invoiceService.payInvoice(id);

        return new ApiResponse<>(true, "Invoice paid", null);
    }
}