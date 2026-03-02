package com.revpay.controller.view;

import com.revpay.dto.response.PageResponse;
import com.revpay.service.interfaces.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionViewController {

    // Service responsible for handling transaction-related operations
    @Autowired
    private TransactionService transactionService;

    /**
     * Handles GET request to "/transactions"
     *
     * Function:
     * - Displays paginated transaction history for the logged-in user.
     * - Supports optional filtering:
     *      1. Page number (default = 0)
     *      2. Page size (default = 5)
     *      3. Transaction type (e.g., CREDIT / DEBIT)
     *      4. From date
     *      5. To date
     *
     * - Cleans empty filter parameters by converting blank values to null.
     * - Calls service layer to search/filter transactions.
     * - Adds transaction list and pagination data to the model.
     * - Returns "transactions.html" view.
     */
    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size,
            @RequestParam(required=false) String type,
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to,
            Model model) {

        // Convert blank values to null (to avoid incorrect filtering)
        from = (from == null || from.isBlank()) ? null : from;
        to   = (to == null || to.isBlank()) ? null : to;
        type = (type == null || type.isBlank()) ? null : type;

        // Fetch paginated and filtered transactions
        PageResponse<?> response =
                transactionService.searchTransactions(page, size, type, from, to, null);

        // Add transaction data to model
        model.addAttribute("txns", response.getContent());

        // Add pagination details
        model.addAttribute("currentPage", response.getCurrentPage());
        model.addAttribute("totalPages", response.getTotalPages());

        return "transactions";
    }
}