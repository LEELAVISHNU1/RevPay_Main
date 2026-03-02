package com.revpay.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.revpay.dto.request.LoginRequest;
import com.revpay.dto.request.RegisterRequest;
import jakarta.validation.Valid;

@Controller
public class AuthViewController {

    // Injecting AuthenticationManager to manually authenticate user credentials
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Handles POST request for user login.
     * 
     * Steps performed:
     * 1. Validates login form input using @Valid.
     * 2. If validation errors exist, returns login page again.
     * 3. Authenticates user using AuthenticationManager.
     * 4. If authentication succeeds:
     *      - Stores authentication in SecurityContext.
     *      - Redirects user to dashboard.
     * 5. If authentication fails:
     *      - Catches BadCredentialsException.
     *      - Adds error message to model.
     *      - Returns login page without redirecting.
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("login") LoginRequest request,
                        BindingResult result,
                        Model model) {

        // If form validation fails, stay on login page
        if (result.hasErrors()) {
            return "login";   // VERY IMPORTANT: return view name, not redirect
        }

        try {
            // Authenticate user using username and password
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Store authentication in SecurityContext (marks user as logged in)
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Redirect to dashboard after successful login
            return "redirect:/dashboard";

        } catch (BadCredentialsException e) {

            // If credentials are invalid, show error message
            model.addAttribute("error", "Invalid email or password");

            // Return login page again without redirect
            return "login";
        }
    }

    /**
     * Handles GET request for login page.
     * 
     * Adds an empty LoginRequest object to the model.
     * This is required for form binding in Thymeleaf.
     * 
     * Returns the login.html view.
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("login", new LoginRequest());
        return "login";
    }

    /**
     * Handles GET request for registration page.
     * 
     * Adds an empty RegisterRequest object to the model.
     * This allows Thymeleaf form fields to bind properly.
     * 
     * Returns the register.html view.
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }
}