package com.revpay.controller.view;

import com.revpay.service.interfaces.PaymentMethodService;
import com.revpay.service.interfaces.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TransferViewController {

    // Service responsible for wallet-based money transfers
    @Autowired
    private WalletService walletService;

    // Service used to fetch user's linked cards
    @Autowired
    private PaymentMethodService paymentMethodService;

    /**
     * Handles GET request to "/send"
     *
     * Function:
     * - Opens the send money page.
     * - Fetches all saved cards of the logged-in user.
     * - Adds cards list to the model (for card payment option).
     * - Returns "send-money.html" view.
     */
    @GetMapping("/send")
    public String sendPage(Model model) {

        // Add user's saved cards to UI
        model.addAttribute("cards", paymentMethodService.myCards());

        return "send-money";
    }

    /**
     * Handles POST request to "/send"
     *
     * Function:
     * - Receives:
     *      1. Receiver email
     *      2. Amount
     *      3. Remark/message
     *      4. Optional cardId (if sending via card)
     *      5. Transaction PIN (for wallet transfer)
     *
     * - If cardId is provided:
     *      → Money is sent using selected card.
     * - If cardId is NOT provided:
     *      → Money is sent using wallet (PIN verification required).
     *
     * - On success:
     *      → Shows success message.
     * - On failure:
     *      → Shows error message.
     *
     * - Redirects to dashboard.
     */
    @PostMapping("/send")
    public String sendMoney(@RequestParam String email,
                            @RequestParam Double amount,
                            @RequestParam String remark,
                            @RequestParam(required = false) Long cardId,
                            @RequestParam String pin,
                            RedirectAttributes redirectAttributes) {

        try {

            // If card is selected → send money using card
            if (cardId != null)
                walletService.sendMoneyUsingCard(cardId, email, amount, remark);

            else
                // Otherwise send using wallet (requires PIN verification)
                walletService.sendMoney(email, amount, remark, pin);

            redirectAttributes.addFlashAttribute("success", "Money sent successfully");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }
}