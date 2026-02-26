package com.revpay.controller.view;

import com.revpay.entity.PaymentMethod;
import com.revpay.service.interfaces.InvoiceService;
import com.revpay.service.interfaces.PaymentMethodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class InvoiceViewController {

    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private PaymentMethodService paymentMethodService;

    // business create page
    @GetMapping("/invoice/create")
    public String createPage() {
        return "create-invoice";
    }

    // create invoice
    @PostMapping("/invoice/create")
    public String create(@RequestParam String customerEmail,
                         @RequestParam Double amount,
                         @RequestParam String description,
                         @RequestParam String dueDate,
                         RedirectAttributes ra) {

        try {
            invoiceService.createInvoice(
                    customerEmail,
                    amount,
                    description,
                    LocalDate.parse(dueDate)
            );

            ra.addFlashAttribute("success", "Invoice created successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // customer view invoices
 // customer view invoices
    @GetMapping("/invoices")
    public String myInvoices(Model model) {

        model.addAttribute("invoices", invoiceService.myReceivedInvoices());
        model.addAttribute("cards", paymentMethodService.myCards());

        return "invoices";
    }

    // pay invoice
    @PostMapping("/invoice/pay/{id}")
    public String pay(@PathVariable Long id,
                      @RequestParam(required = false) Long cardId,
                      RedirectAttributes ra) {

        try {

            if (cardId != null)
                invoiceService.payInvoiceUsingCard(id, cardId);
            else
                invoiceService.payInvoice(id);

            ra.addFlashAttribute("success", "Invoice paid successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/invoices";
    }
}
