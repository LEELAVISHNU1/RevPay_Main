package com.revpay.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.revpay.entity.User;
import com.revpay.repository.UserRepository;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * Purpose:
 * - Used by Spring Security during authentication.
 * - Loads user details from the database.
 * - Converts User entity into CustomUserDetails object.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Repository used to fetch user from database
    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user by username (email in your case).
     *
     * Function:
     * - Called automatically by Spring Security during login.
     * - Finds user by email.
     * - If user not found → throws UsernameNotFoundException.
     * - If found → wraps user inside CustomUserDetails object.
     *
     * @param username → Email entered during login
     * @return UserDetails object used by Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Fetch user from database using email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Wrap User entity into CustomUserDetails
        return new CustomUserDetails(user);
    }
}