package com.revpay.controller.view;

import com.revpay.dto.request.RegisterRequest;
import com.revpay.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterViewController {

    // Service responsible for handling registration logic
    @Autowired
    private AuthService authService;

    /**
     * Handles POST request to "/register"
     *
     * Function:
     * - Receives registration form data.
     * - Performs validation using @Valid.
     * - If validation errors exist:
     *      → Returns "register" page again.
     * - If validation passes:
     *      → Calls AuthService to register the user.
     * - On successful registration:
     *      → Adds success message.
     *      → Returns login page.
     * - If exception occurs (e.g., email exists):
     *      → Adds error message.
     *      → Returns register page.
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterRequest request,
                           BindingResult result,
                           Model model) {

        // If form validation fails, stay on register page
        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Call service layer to create new user
            authService.register(request);

            // Add success message
            model.addAttribute("success", "Registration successful. Please login.");

            // Redirect user to login page
            return "login";

        } catch (Exception e) {

            // If registration fails (e.g., duplicate email)
            model.addAttribute("error", e.getMessage());

            return "register";
        }
    }
}