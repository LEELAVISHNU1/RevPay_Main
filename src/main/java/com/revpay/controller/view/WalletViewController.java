package com.revpay.controller.view;

import com.revpay.service.interfaces.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WalletViewController {

    // Service responsible for wallet-related operations
    @Autowired
    private WalletService walletService;

    /**
     * Handles GET request to "/wallet/add"
     *
     * Function:
     * - Displays the add money page.
     * - Allows user to enter amount and remark.
     * - Returns "add-money.html" view.
     */
    @GetMapping("/wallet/add")
    public String addMoneyPage() {
        return "add-money";
    }

    /**
     * Handles POST request to "/wallet/add"
     *
     * Function:
     * - Receives amount and remark from form.
     * - Calls service layer to add money to user's wallet.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects to dashboard.
     */
    @PostMapping("/wallet/add")
    public String addMoney(@RequestParam Double amount,
                           @RequestParam String remark,
                           RedirectAttributes ra) {

        try {
            // Add money to logged-in user's wallet
            walletService.addMoney(amount, remark);

            ra.addFlashAttribute("success","Money added successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }
}