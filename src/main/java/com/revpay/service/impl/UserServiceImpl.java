package com.revpay.service.impl;

import com.revpay.entity.User;
import com.revpay.repository.UserRepository;
import com.revpay.security.SecurityUtil;
import com.revpay.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service implementation for handling user-related operations.
 *
 * Primary responsibility:
 * - Fetch the currently authenticated user from the database.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the currently logged-in user.
     *
     * Flow:
     * 1. Gets the current authenticated username (email) from SecurityContext.
     * 2. Fetches the User entity from the database using that email.
     * 3. Throws RuntimeException if user does not exist.
     *
     * Used throughout the system in:
     * - Wallet operations
     * - Transactions
     * - Loans
     * - Invoices
     * - Requests
     * - Notifications
     *
     * @return User entity of the currently authenticated user
     */
    @Override
    public User getCurrentUser() {

        // Get email (username) from Spring Security context
        String username = SecurityUtil.getCurrentUsername();

        // Fetch user from database using email
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}