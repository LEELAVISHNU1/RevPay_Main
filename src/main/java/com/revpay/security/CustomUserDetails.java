package com.revpay.security;

import com.revpay.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    // Stores the authenticated user entity for Spring Security
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Returns the wrapped User entity
    public User getUser() {
        return user;
    }

    // Provides the user's role as a Spring Security authority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName())
        );
    }

    // Returns the encoded password for authentication
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Returns email as the username for login
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // Indicates the account is not expired
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Indicates the account is not locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Indicates the credentials are valid and not expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Indicates the user account is active
    @Override
    public boolean isEnabled() {
        return true;
    }
}