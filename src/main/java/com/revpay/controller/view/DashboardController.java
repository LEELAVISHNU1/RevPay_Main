package com.revpay.controller.view;

import com.revpay.entity.User;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import com.revpay.service.interfaces.InvoiceService;
import com.revpay.service.interfaces.LoanService;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.PaymentMethodService;
import com.revpay.service.interfaces.RequestService;
import com.revpay.service.interfaces.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RequestService requestService;

    // Loads dashboard with user details and role-based data (admin, personal, business)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        User user = userService.getCurrentUser();

        String role = user.getRole().getRoleName();

        model.addAttribute("name", user.getFullName());

        model.addAttribute("cards",
                paymentMethodService.myCards());

        model.addAttribute("incomingRequests",
                requestService.getIncomingRequests());

        model.addAttribute("sentRequests",
                requestService.mySentRequests());

        if (role.equals("ADMIN")) {

            model.addAttribute("loans", loanService.pendingLoans());

            return "dashboard";
        }

        if(user.getRole().getRoleName().equals("BUSINESS")) {

            model.addAttribute("analytics",
                    invoiceService.getBusinessAnalytics());
        }

        if(user.getRole().getRoleName().equals("BUSINESS")) {

            model.addAttribute("createdInvoices",
                    invoiceService.myCreatedInvoices());
        }

        if (role.equals("PERSONAL") || role.equals("BUSINESS")) {

            model.addAttribute("notifications", notificationService.getAllNotifications());

            model.addAttribute("balance", walletService.getMyWallet().getBalance());

            model.addAttribute("transactions",
                    transactionService.myTransactions(0,5).getContent());

            model.addAttribute("loans",
                    loanService.myActiveLoans());

            return "dashboard";
        }

        return "dashboard";
    }
}