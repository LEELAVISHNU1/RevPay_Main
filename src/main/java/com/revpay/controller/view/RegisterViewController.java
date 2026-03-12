package com.revpay.controller.view;

import com.revpay.dto.request.LoginRequest;
import com.revpay.dto.request.RegisterRequest;
import com.revpay.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterViewController {

    @Autowired
    private AuthService authService;

    // Handles user registration with validation and redirects to login on success
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterRequest request,
                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.register(request);

            model.addAttribute("success", "Registration successful. Please login.");

            model.addAttribute("login", new LoginRequest());

            return "login";

        } catch (Exception e) {

            model.addAttribute("error", e.getMessage());

            return "register";
        }
    }
}