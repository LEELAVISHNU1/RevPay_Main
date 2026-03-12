package com.revpay.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    // Returns email of the currently authenticated user
    public static String getCurrentUsername() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated())
            return null;

        return auth.getName();
    }
}