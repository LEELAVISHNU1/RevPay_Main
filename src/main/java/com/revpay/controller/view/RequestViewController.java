package com.revpay.controller.view;

import com.revpay.service.interfaces.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestViewController {

    // Service responsible for handling money request logic
    @Autowired
    private RequestService requestService;

    /**
     * Handles GET request to "/request"
     *
     * Function:
     * - Displays the request money page.
     * - Allows user to enter email, amount, and note.
     * - Returns "request-money.html" view.
     */
    @GetMapping("/request")
    public String requestPage() {
        return "request-money";
    }

    /**
     * Handles POST request to "/request"
     *
     * Function:
     * - Receives receiver email, amount, and note.
     * - Calls service layer to create money request.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects to dashboard.
     */
    @PostMapping("/request")
    public String createRequest(@RequestParam String email,
                                @RequestParam Double amount,
                                @RequestParam String note,
                                RedirectAttributes ra) {

        try {
            // Create a new money request
            requestService.createRequest(email, amount, note);

            ra.addFlashAttribute("success", "Money request sent successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /**
     * Handles GET request to "/requests"
     *
     * Function:
     * - Fetches all incoming money requests for logged-in user.
     * - Adds them to model.
     * - Returns "incoming-requests.html" view.
     */
    @GetMapping("/requests")
    public String incomingRequests(Model model) {

        model.addAttribute("requests", requestService.getIncomingRequests());

        return "incoming-requests";
    }

    /**
     * Handles POST request to "/requests/accept/{id}"
     *
     * Function:
     * - Accepts a specific money request using its ID.
     * - Transfers money from current user to requester.
     * - Shows success or error message.
     * - Redirects back to incoming requests page.
     */
    @PostMapping("/requests/accept/{id}")
    public String accept(@PathVariable Long id, RedirectAttributes ra) {

        try {
            requestService.acceptRequest(id);
            ra.addFlashAttribute("success", "Request accepted");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/requests";
    }

    /**
     * Handles POST request to "/requests/reject/{id}"
     *
     * Function:
     * - Rejects a specific money request.
     * - Updates request status to rejected.
     * - Shows success or error message.
     * - Redirects back to incoming requests page.
     */
    @PostMapping("/requests/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {

        try {
            requestService.rejectRequest(id);
            ra.addFlashAttribute("success", "Request rejected");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/requests";
    }
}