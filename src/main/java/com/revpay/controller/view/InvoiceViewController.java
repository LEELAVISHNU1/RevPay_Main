package com.revpay.controller.view;

//import com.revpay.entity.PaymentMethod;
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

    // Service responsible for handling invoice-related business logic
    @Autowired
    private InvoiceService invoiceService;

    // Service used to fetch user’s linked payment cards
    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * Handles GET request to "/invoice/create"
     *
     * Function:
     * - Displays the invoice creation page.
     * - Used by BUSINESS users to generate invoices.
     * - Returns "create-invoice.html" view.
     */
    @GetMapping("/invoice/create")
    public String createPage() {
        return "create-invoice";
    }

    /**
     * Handles POST request to "/invoice/create"
     *
     * Function:
     * - Receives invoice details from form.
     * - Calls service layer to create and store invoice.
     * - Converts dueDate from String to LocalDate.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects user to dashboard.
     */
    @PostMapping("/invoice/create")
    public String create(@RequestParam String customerEmail,
                         @RequestParam Double amount,
                         @RequestParam String description,
                         @RequestParam String dueDate,
                         RedirectAttributes ra) {

        try {
            // Create invoice using provided details
            invoiceService.createInvoice(
                    customerEmail,
                    amount,
                    description,
                    LocalDate.parse(dueDate) // Convert String to LocalDate
            );

            // Success message
            ra.addFlashAttribute("success", "Invoice created successfully");

        } catch (Exception e) {

            // Error message if invoice creation fails
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /**
     * Handles GET request to "/invoices"
     *
     * Function:
     * - Fetches all invoices received by the logged-in user.
     * - Also fetches user’s saved payment cards.
     * - Used by customers to view and pay invoices.
     * - Returns "invoices.html" view.
     */
    @GetMapping("/invoices")
    public String myInvoices(Model model) {

        // Add received invoices to model
        model.addAttribute("invoices", invoiceService.myReceivedInvoices());

        // Add user's saved cards for payment option
        model.addAttribute("cards", paymentMethodService.myCards());

        return "invoices";
    }

    /**
     * Handles POST request to "/invoice/pay/{id}"
     *
     * Function:
     * - Pays selected invoice.
     * - If cardId is provided → payment happens via card.
     * - If cardId is null → payment happens via wallet.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects back to invoices page.
     */
    @PostMapping("/invoice/pay/{id}")
    public String pay(@PathVariable Long id,
                      @RequestParam(required = false) Long cardId,
                      RedirectAttributes ra) {

        try {

            // If cardId exists, pay using card
            if (cardId != null)
                invoiceService.payInvoiceUsingCard(id, cardId);
            else
                // Otherwise pay using wallet
                invoiceService.payInvoice(id);

            ra.addFlashAttribute("success", "Invoice paid successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/invoices";
    }
}