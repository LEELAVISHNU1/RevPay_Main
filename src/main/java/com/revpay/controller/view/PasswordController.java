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

	
	 @Autowired
	    private UserRepository userRepository;

	    @Autowired
	    private PasswordEncoder passwordEncoder;

	    @GetMapping("/forgot-password")
	    public String forgotPasswordPage() {
	        return "forgot-password";
	    }

	    @PostMapping("/verify-color")
	    public String verifyColor(@RequestParam String email,
	                              @RequestParam String favoriteColor,
	                              Model model) {

	        User user = userRepository.findByEmail(email)
	                .orElse(null);

	        if (user == null ||
	            !user.getFavoriteColor().equalsIgnoreCase(favoriteColor)) {

	            model.addAttribute("error", "Invalid email or favorite color");
	            return "forgot-password";
	        }

	        model.addAttribute("email", email);
	        return "reset-password";
	    }

	    @PostMapping("/reset-password")
	    public String resetPassword(@RequestParam String email,
	                                @RequestParam String password,
	                                RedirectAttributes ra) {

	        User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        user.setPassword(passwordEncoder.encode(password));
	        userRepository.save(user);

	        ra.addFlashAttribute("success", "Password updated successfully!");
	        return "redirect:/login";
	    }
}