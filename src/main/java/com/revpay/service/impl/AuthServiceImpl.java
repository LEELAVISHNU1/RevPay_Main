package com.revpay.service.impl;

import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.revpay.config.SecurityConfig;
import com.revpay.dto.request.RegisterRequest;
import com.revpay.entity.Role;
import com.revpay.entity.User;
import com.revpay.repository.RoleRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.AuthService;
import com.revpay.service.interfaces.WalletService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

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

    public AuthServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    // Registers a new user, validates input, and creates wallet
    @Override
    public void register(RegisterRequest request) {

        logger.info("Registration attempt started for email: {}", request.getEmail());

        try {

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("Registration failed - Email already registered: {}", request.getEmail());
                throw new RuntimeException("Email already registered");
            }

            Role role = roleRepository.findByRoleName(request.getRole())
                    .orElseThrow(() -> {
                        logger.warn("Registration failed - Role not found: {}", request.getRole());
                        return new RuntimeException("Role not found");
                    });

            if (userRepository.existsByPhone(request.getPhone())) {
                logger.warn("Registration failed - Phone already registered: {}", request.getPhone());
                throw new RuntimeException("Phone number already registered");
            }

            User user = new User();

            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());

            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setTransactionPin(passwordEncoder.encode(request.getTransactionPin()));

            user.setRole(role);
            user.setFavoriteColor(request.getFavoriteColor());
            user.setAccountStatus("ACTIVE");
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);
            logger.info("User saved successfully in database: {}", user.getEmail());

            walletService.createWallet(user);
            logger.info("Wallet created successfully for user: {}", user.getEmail());

            logger.info("Registration completed successfully for email: {}", request.getEmail());

        } catch (RuntimeException ex) {

            logger.error("Registration error for email: {} | Reason: {}",
                    request.getEmail(), ex.getMessage());

            throw ex;

        } catch (Exception ex) {

            logger.error("Unexpected error during registration for email: {}",
                    request.getEmail(), ex);

            throw new RuntimeException("Registration failed");
        }
    }
}