package com.revpay.security;

import com.revpay.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Custom implementation of Spring Security's UserDetails.
 *
 * Purpose:
 * - Acts as a bridge between your application's User entity
 *   and Spring Security authentication system.
 * - Wraps the User object.
 * - Provides user information to Spring Security during login.
 */
public class CustomUserDetails implements UserDetails {

    // The actual User entity from database
    private final User user;

    /**
     * Constructor
     *
     * Function:
     * - Accepts User entity and assigns it to this class.
     * - Used during authentication process.
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns the wrapped User entity.
     *
     * Function:
     * - Allows access to full User object when needed.
     */
    public User getUser() {
        return user;
    }

    /**
     * ⭐ VERY IMPORTANT FOR ROLE-BASED SECURITY
     *
     * Function:
     * - Returns user's authorities (roles).
     * - Spring Security expects roles to start with "ROLE_".
     * - Example:
     *      If roleName = "ADMIN"
     *      Authority becomes = "ROLE_ADMIN"
     *
     * Used for:
     * - @PreAuthorize
     * - Role-based URL restrictions
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())
        );
    }

    /**
     * Returns user's encoded password.
     *
     * Used by Spring Security during authentication.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns username used for login.
     *
     * Here:
     * - Email is used as username.
     * - Also used as JWT subject.
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Indicates whether the account has expired.
     *
     * Returning true means:
     * - Account is valid and not expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is locked.
     *
     * Returning true means:
     * - Account is not locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether user credentials (password) are expired.
     *
     * Returning true means:
     * - Credentials are valid.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     *
     * Returning true means:
     * - User account is active.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}