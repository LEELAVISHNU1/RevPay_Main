package com.revpay.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * Purpose:
 * - Intercepts every incoming HTTP request.
 * - Extracts JWT token from Authorization header.
 * - Validates the token.
 * - If valid → sets authentication in Spring Security context.
 *
 * Extends OncePerRequestFilter:
 * - Ensures filter runs only once per request.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Utility class for extracting and validating JWT tokens
    @Autowired
    private JwtUtil jwtUtil;

    // Loads user details from database
    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Core filtering logic.
     *
     * Flow:
     * 1. Check if Authorization header exists.
     * 2. Extract token (after "Bearer ").
     * 3. Extract username from token.
     * 4. Validate token.
     * 5. If valid → set authentication in SecurityContext.
     * 6. Continue filter chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header
        String header = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (header != null && header.startsWith("Bearer ")) {

            // Extract token (remove "Bearer ")
            String token = header.substring(7);

            // Extract username (email) from token
            String username = jwtUtil.extractUsername(token);

            // If username exists and user not already authenticated
            if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from database
                CustomUserDetails userDetails =
                        (CustomUserDetails) userDetailsService.loadUserByUsername(username);

                // Validate token with username
                if (jwtUtil.validateToken(token, username)) {

                    // Create authentication token with roles
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities() // ⭐ roles injected here
                            );

                    // Attach request details
                    auth.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}