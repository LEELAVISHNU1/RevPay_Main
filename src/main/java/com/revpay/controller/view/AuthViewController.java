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

    @Autowired
    private AuthenticationManager authenticationManager;

    // Processes login form, authenticates user, and redirects to dashboard on success
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("login") LoginRequest request,
                        BindingResult result,
                        Model model) {

        if (result.hasErrors()) {
            return "login";
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            return "redirect:/dashboard";

        } catch (BadCredentialsException e) {

            model.addAttribute("error", "Invalid email or password");

            return "login";
        }
    }

    // Loads the login page and adds an empty LoginRequest for form binding
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("login", new LoginRequest());
        return "login";
    }

    // Loads the registration page and adds an empty RegisterRequest for the form
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }
}