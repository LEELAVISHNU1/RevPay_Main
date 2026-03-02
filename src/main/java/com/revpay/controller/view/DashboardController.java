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

    // Service to get currently logged-in user details
    @Autowired
    private UserService userService;

    // Service to manage wallet operations
    @Autowired
    private WalletService walletService;

    // Service to fetch transaction history
    @Autowired
    private TransactionService transactionService;

    // Service to manage loan-related operations
    @Autowired
    private LoanService loanService;

    // Service to manage notifications
    @Autowired
    private NotificationService notificationService;

    // Service to manage user payment cards
    @Autowired
    private PaymentMethodService paymentMethodService;

    // Service to manage invoices (mainly for BUSINESS users)
    @Autowired
    private InvoiceService invoiceService;

    // Service to manage money requests
    @Autowired
    private RequestService requestService;


    /**
     * Handles GET request for "/dashboard"
     *
     * Function:
     * - Fetches currently logged-in user.
     * - Identifies user role (ADMIN, PERSONAL, BUSINESS).
     * - Adds common and role-specific data to the model.
     * - Returns dashboard view.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Get logged-in user
        User user = userService.getCurrentUser();

        // Get user role
        String role = user.getRole().getRoleName();

        // Add user's full name to dashboard
        model.addAttribute("name", user.getFullName());

        // Add user's linked cards
        model.addAttribute("cards",
                paymentMethodService.myCards());

        // Add incoming money requests (others requested money from current user)
        model.addAttribute("incomingRequests",
                requestService.getIncomingRequests());

        // Add sent money requests (current user requested money from others)
        model.addAttribute("sentRequests",
                requestService.mySentRequests());

        // ================= ADMIN SECTION =================
        if (role.equals("ADMIN")) {

            // Admin sees all pending loan applications
            model.addAttribute("loans", loanService.pendingLoans());

            return "dashboard";
        }

        // ================= BUSINESS SPECIFIC FEATURES =================
        if(user.getRole().getRoleName().equals("BUSINESS")) {

            // Add business invoice analytics (revenue, stats, etc.)
            model.addAttribute("analytics",
                    invoiceService.getBusinessAnalytics());
        }

        if(user.getRole().getRoleName().equals("BUSINESS")) {

            // Add invoices created by business user
            model.addAttribute("createdInvoices",
                    invoiceService.myCreatedInvoices());
        }

        // ================= PERSONAL + BUSINESS COMMON FEATURES =================
        if (role.equals("PERSONAL") || role.equals("BUSINESS")) {

            // Add notifications for user
            model.addAttribute("notifications", notificationService.myNotifications());

            // Add current wallet balance
            model.addAttribute("balance", walletService.getMyWallet().getBalance());

            // Add last 5 recent transactions
            model.addAttribute("transactions",
                    transactionService.myTransactions(0,5).getContent());

            // Add active loans of the user
            model.addAttribute("loans",
                    loanService.myActiveLoans());

            return "dashboard";
        }

        // Default fallback (should normally not reach here)
        return "dashboard";
    }
}