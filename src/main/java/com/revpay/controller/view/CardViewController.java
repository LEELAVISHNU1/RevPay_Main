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

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private WalletService walletService;

    // Loads all cards of the logged-in user and displays them
    @GetMapping
    public String myCards(Model model) {
        model.addAttribute("cards", paymentMethodService.myCards());
        return "cards";
    }

    // Opens the page to add a new card
    @GetMapping("/add")
    public String addCardPage() {
        return "add-card";
    }

    // Saves a new card and shows success or error message
    @PostMapping("/add")
    public String addCard(@RequestParam String cardNumber,
                          @RequestParam String holderName,
                          @RequestParam String expiry,
                          @RequestParam String cvv,
                          RedirectAttributes ra) {

        try {
            paymentMethodService.addCard(cardNumber, holderName, expiry, cvv);
            ra.addFlashAttribute("success", "Card added successfully");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cards/add";
    }

    // Adds money to wallet using the selected card
    @PostMapping("/deposit")
    public String deposit(@RequestParam Long cardId,
                          @RequestParam Double amount,
                          RedirectAttributes ra) {

        try {
            walletService.addMoneyViaCard(cardId, amount);
            ra.addFlashAttribute("success", "Money added via card");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // Deletes a card by its ID and redirects to cards page
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {

        try {
            paymentMethodService.deleteCard(id);
            ra.addFlashAttribute("success", "Card removed");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cards";
    }
}