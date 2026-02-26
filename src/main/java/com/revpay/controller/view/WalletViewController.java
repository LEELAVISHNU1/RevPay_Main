package com.revpay.controller.view;

import com.revpay.service.interfaces.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WalletViewController {

    @Autowired
    private WalletService walletService;

    // open add money page
    @GetMapping("/wallet/add")
    public String addMoneyPage() {
        return "add-money";
    }

    // submit add money
    @PostMapping("/wallet/add")
    public String addMoney(@RequestParam Double amount,
                           @RequestParam String remark,
                           RedirectAttributes ra) {

        try {
            walletService.addMoney(amount, remark);
            ra.addFlashAttribute("success","Money added successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }
}