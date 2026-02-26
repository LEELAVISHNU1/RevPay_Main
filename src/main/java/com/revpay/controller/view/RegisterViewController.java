package com.revpay.controller.view;

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
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterRequest request,
                           BindingResult result,
                           Model model) {

        // validation errors
        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.register(request);
            model.addAttribute("success", "Registration successful. Please login.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}