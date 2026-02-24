package com.revpay.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.revpay.config.SecurityConfig;

import com.revpay.dto.request.LoginRequest;
import com.revpay.dto.request.RegisterRequest;
import com.revpay.entity.Role;
import com.revpay.entity.User;
import com.revpay.repository.RoleRepository;
import com.revpay.repository.UserRepository;
import com.revpay.security.CustomUserDetails;
import com.revpay.service.interfaces.AuthService;
import com.revpay.service.interfaces.WalletService;

@Service
public class AuthServiceImpl implements AuthService {
    private final SecurityConfig securityConfig;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private WalletService walletService;

    
    @Autowired
    private AuthenticationManager authenticationManager;
    AuthServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }


    @Override
    public void register(RegisterRequest request) {

        Role role = roleRepository.findByRoleName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        walletService.createWallet(user);
    }

//    @Override
//    public User login(LoginRequest request) {
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getUsername(),
//                        request.getPassword()
//                )
//        );
//
//        // ⭐ THIS LINE CREATES LOGIN SESSION
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        return userDetails.getUser();
//    }

}

