package com.revpay.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security Utility Class
 *
 * Purpose:
 * - Provides helper methods related to Spring Security context.
 * - Used to retrieve information about the currently logged-in user.
 */
@Component
public class SecurityUtil {

    /**
     * Returns the username (email) of the currently authenticated user.
     *
     * Function:
     * - Retrieves Authentication object from SecurityContextHolder.
     * - Checks if authentication exists and user is authenticated.
     * - Returns username (which is email in your application).
     * - Returns null if no user is authenticated.
     *
     * Used in:
     * - Service layer
     * - Getting current logged-in user
     * - Auditing or tracking operations
     *
     * @return Logged-in user's email OR null if not authenticated
     */
    public static String getCurrentUsername() {

        // Get authentication object from security context
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // If no authentication or not authenticated → return null
        if (auth == null || !auth.isAuthenticated())
            return null;

        // Return username (email in your case)
        return auth.getName();
    }
}