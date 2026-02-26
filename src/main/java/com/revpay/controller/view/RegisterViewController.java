package com.revpay.controller.view;

import com.revpay.dto.request.RegisterRequest;
import com.revpay.service.interfaces.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterViewController {

    @Autowired
    private AuthService authService;

//    @PostMapping("/register")
//    public String register(@RequestParam String fullName,
//                           @RequestParam String email,
//                           @RequestParam String phone,
//                           @RequestParam String password,
//                           @RequestParam String role,
//                           RedirectAttributes ra) {
//
//        try {
//            // ⭐ Convert form fields → DTO
//            RegisterRequest request = new RegisterRequest();
//            request.setFullName(fullName);
//            request.setEmail(email);
//            request.setPhone(phone);
//            request.setPassword(password);
//            request.setRole(role);
//
//            authService.register(request);
//
//            ra.addFlashAttribute("success", "Registration successful. Please login.");
//            return "redirect:/login";
//
//        } catch (Exception e) {
//            ra.addFlashAttribute("error", e.getMessage());
//            return "redirect:/register";
//        }
//    }
    
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