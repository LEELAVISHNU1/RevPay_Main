package com.revpay.controller.view;

import com.revpay.dto.response.PageResponse;
import com.revpay.service.interfaces.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionViewController {

    @Autowired
    private TransactionService transactionService;

    // Shows paginated transaction history with optional filters
    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size,
            @RequestParam(required=false) String type,
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to,
            Model model) {

        from = (from == null || from.isBlank()) ? null : from;
        to   = (to == null || to.isBlank()) ? null : to;
        type = (type == null || type.isBlank()) ? null : type;

        PageResponse<?> response =
                transactionService.searchTransactions(page, size, type, from, to, null);

        model.addAttribute("txns", response.getContent());

        model.addAttribute("currentPage", response.getCurrentPage());
        model.addAttribute("totalPages", response.getTotalPages());

        return "transactions";
    }
}