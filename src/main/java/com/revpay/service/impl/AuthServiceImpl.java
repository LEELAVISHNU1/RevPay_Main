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

/**
 * Implementation of AuthService.
 *
 * Purpose:
 * - Handles user registration logic.
 * - Performs validations.
 * - Saves user into database.
 * - Creates wallet for new user.
 * - Logs all important events.
 */
@Service
public class AuthServiceImpl implements AuthService {

    // Logger for recording registration events
    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    // Security configuration (injected through constructor)
    private final SecurityConfig securityConfig;

    // Repository to manage User entity
    @Autowired
    private UserRepository userRepository;

    // Repository to fetch roles from database
    @Autowired
    private RoleRepository roleRepository;

    // Used to encode password and transaction PIN
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Used to create wallet after successful registration
    @Autowired
    private WalletService walletService;

    // Used for authentication (not directly used here but available)
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Constructor-based injection for SecurityConfig.
     */
    public AuthServiceImpl(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Registers a new user.
     *
     * Flow:
     * 1. Log registration attempt.
     * 2. Check if email already exists.
     * 3. Validate role.
     * 4. Check if phone already exists.
     * 5. Create new User entity.
     * 6. Encode password and transaction PIN.
     * 7. Save user in database.
     * 8. Create wallet for the user.
     * 9. Log success.
     *
     * Error Handling:
     * - Business errors → RuntimeException.
     * - Unexpected errors → Generic registration failure.
     */
    @Override
    public void register(RegisterRequest request) {

        // Log registration start
        logger.info("Registration attempt started for email: {}", request.getEmail());

        try {

            // ================= EMAIL CHECK =================
            // Check if user with same email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("Registration failed - Email already registered: {}", request.getEmail());
                throw new RuntimeException("Email already registered");
            }

            // ================= ROLE VALIDATION =================
            // Fetch role from database
            Role role = roleRepository.findByRoleName(request.getRole())
                    .orElseThrow(() -> {
                        logger.warn("Registration failed - Role not found: {}", request.getRole());
                        return new RuntimeException("Role not found");
                    });

            // ================= PHONE CHECK =================
            // Ensure phone number is unique
            if (userRepository.existsByPhone(request.getPhone())) {
                logger.warn("Registration failed - Phone already registered: {}", request.getPhone());
                throw new RuntimeException("Phone number already registered");
            }

            // ================= CREATE USER =================
            User user = new User();

            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());

            // 🔐 IMPORTANT: Encode password and PIN before saving
            // Never store plain text passwords
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setTransactionPin(passwordEncoder.encode(request.getTransactionPin()));

            user.setRole(role);
            user.setFavoriteColor(request.getFavoriteColor());
            user.setAccountStatus("ACTIVE");
            user.setCreatedAt(LocalDateTime.now());

            // Save user to database
            userRepository.save(user);
            logger.info("User saved successfully in database: {}", user.getEmail());

            // ================= CREATE WALLET =================
            walletService.createWallet(user);
            logger.info("Wallet created successfully for user: {}", user.getEmail());

            logger.info("Registration completed successfully for email: {}", request.getEmail());

        } catch (RuntimeException ex) {

            // Log business-level errors
            logger.error("Registration error for email: {} | Reason: {}",
                    request.getEmail(), ex.getMessage());

            throw ex;

        } catch (Exception ex) {

            // Log unexpected errors
            logger.error("Unexpected error during registration for email: {}",
                    request.getEmail(), ex);

            throw new RuntimeException("Registration failed");
        }
    }
}