package com.revpay.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.revpay.service.interfaces.PaymentMethodService;
import com.revpay.service.interfaces.WalletService;

@Controller
@RequestMapping("/cards")
public class CardViewController {

    // Service used to manage card-related operations
    @Autowired
    private PaymentMethodService paymentMethodService;

    // Service used to handle wallet-related operations
    @Autowired
    private WalletService walletService;

    /**
     * Handles GET request to "/cards"
     * 
     * Function:
     * - Fetches all cards linked to the currently logged-in user.
     * - Adds the list of cards to the model.
     * - Returns the "cards.html" view.
     */
    @GetMapping
    public String myCards(Model model) {
        model.addAttribute("cards", paymentMethodService.myCards());
        return "cards";
    }

    /**
     * Handles GET request to "/cards/add"
     * 
     * Function:
     * - Simply returns the add-card page.
     * - Displays form to add a new payment card.
     */
    @GetMapping("/add")
    public String addCardPage() {
        return "add-card";
    }

    /**
     * Handles POST request to "/cards/add"
     * 
     * Function:
     * - Receives card details from the form.
     * - Calls service layer to save the card.
     * - If successful → shows success message.
     * - If exception occurs → shows error message.
     * - Redirects back to add-card page.
     */
    @PostMapping("/add")
    public String addCard(@RequestParam String cardNumber,
                          @RequestParam String holderName,
                          @RequestParam String expiry,
                          @RequestParam String cvv,
                          RedirectAttributes ra) {

        try {
            // Calls service to validate and store card details
            paymentMethodService.addCard(cardNumber, holderName, expiry, cvv);

            // Flash success message
            ra.addFlashAttribute("success", "Card added successfully");

        } catch (Exception e) {

            // Flash error message if validation or saving fails
            ra.addFlashAttribute("error", e.getMessage());
        }

        // Redirect back to add-card page
        return "redirect:/cards/add";
    }

    /**
     * Handles POST request to "/cards/deposit"
     * 
     * Function:
     * - Takes card ID and deposit amount.
     * - Transfers money from selected card to wallet.
     * - If successful → shows success message.
     * - If failure → shows error message.
     * - Redirects user to dashboard.
     */
    @PostMapping("/deposit")
    public String deposit(@RequestParam Long cardId,
                          @RequestParam Double amount,
                          RedirectAttributes ra) {

        try {
            // Adds money to wallet using selected card
            walletService.addMoneyViaCard(cardId, amount);

            // Success message
            ra.addFlashAttribute("success", "Money added via card");

        } catch (Exception e) {

            // Error message if insufficient balance or invalid card
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /**
     * Handles POST request to "/cards/delete/{id}"
     * 
     * Function:
     * - Deletes a card using its ID.
     * - Ensures only the owner can delete it (handled in service layer).
     * - Shows success or error message.
     * - Redirects back to cards page.
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {

        try {
            // Calls service to delete the selected card
            paymentMethodService.deleteCard(id);

            // Success message
            ra.addFlashAttribute("success", "Card removed");

        } catch (Exception e) {

            // Error message if unauthorized or card not found
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cards";
    }
}