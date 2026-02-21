package com.revpay.security;

import com.revpay.entity.User;
import com.revpay.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(usernameOrPhone)
                .or(() -> userRepository.findByPhone(usernameOrPhone))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrPhone));

        return new CustomUserDetails(user);
    }
}