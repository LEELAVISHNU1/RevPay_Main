package com.revpay.controller.invoice;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Invoice;
import com.revpay.service.interfaces.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody Map<String, String> body) {

        invoiceService.createInvoice(
                body.get("customerEmail"),
                Double.valueOf(body.get("amount")),
                body.get("description"),
                LocalDate.parse(body.get("dueDate"))
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Invoice created successfully", null)
        );
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<Invoice>>> received() {

        List<Invoice> invoices = invoiceService.myReceivedInvoices();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Received invoices fetched successfully", invoices)
        );
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<ApiResponse<Void>> pay(@PathVariable Long id) {

        invoiceService.payInvoice(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Invoice paid successfully", null)
        );
    }
}