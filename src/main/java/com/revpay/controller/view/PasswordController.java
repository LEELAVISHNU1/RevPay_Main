package com.revpay.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.revpay.entity.User;
import com.revpay.repository.UserRepository;

@Controller
public class PasswordController {

    // Repository used to fetch and update user data from database
    @Autowired
    private UserRepository userRepository;

    // Used to encode the new password securely before saving
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Handles GET request to "/forgot-password"
     *
     * Function:
     * - Displays forgot password page.
     * - Allows user to enter email and favorite color for verification.
     * - Returns "forgot-password.html" view.
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    /**
     * Handles POST request to "/verify-color"
     *
     * Function:
     * - Verifies user's identity using:
     *      1. Email
     *      2. Favorite color (security question)
     * - If email not found OR favorite color does not match:
     *      → Shows error message and returns to forgot-password page.
     * - If verification succeeds:
     *      → Adds email to model.
     *      → Redirects user to reset-password page.
     */
    @PostMapping("/verify-color")
    public String verifyColor(@RequestParam String email,
                              @RequestParam String favoriteColor,
                              Model model) {

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Validate email and favorite color
        if (user == null ||
            !user.getFavoriteColor().equalsIgnoreCase(favoriteColor)) {

            model.addAttribute("error", "Invalid email or favorite color");
            return "forgot-password";
        }

        // If verification successful, pass email to reset page
        model.addAttribute("email", email);

        return "reset-password";
    }

    /**
     * Handles POST request to "/reset-password"
     *
     * Function:
     * - Receives email and new password.
     * - Finds user by email.
     * - Encodes the new password using BCrypt.
     * - Updates password in database.
     * - Adds success message.
     * - Redirects to login page.
     */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String password,
                                RedirectAttributes ra) {

        // Fetch user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encode new password before saving
        user.setPassword(passwordEncoder.encode(password));

        // Save updated user
        userRepository.save(user);

        // Add success message
        ra.addFlashAttribute("success", "Password updated successfully!");

        return "redirect:/login";
    }
}