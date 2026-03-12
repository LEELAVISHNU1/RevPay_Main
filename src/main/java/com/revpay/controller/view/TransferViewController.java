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

    @Autowired
    private WalletService walletService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    // Opens send money page and loads user's saved cards
    @GetMapping("/send")
    public String sendPage(Model model) {

        model.addAttribute("cards", paymentMethodService.myCards());

        return "send-money";
    }

    // Sends money using card if selected, otherwise uses wallet with PIN
    @PostMapping("/send")
    public String sendMoney(@RequestParam String email,
                            @RequestParam Double amount,
                            @RequestParam String remark,
                            @RequestParam(required = false) Long cardId,
                            @RequestParam String pin,
                            RedirectAttributes redirectAttributes) {

        try {

            if (cardId != null)
                walletService.sendMoneyUsingCard(cardId, email, amount, remark);
            else
                walletService.sendMoney(email, amount, remark, pin);

            redirectAttributes.addFlashAttribute("success", "Money sent successfully");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }
}