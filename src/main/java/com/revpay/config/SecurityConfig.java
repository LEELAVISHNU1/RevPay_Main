package com.revpay.config;

import com.revpay.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.revpay.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Injecting custom JWT authentication filter
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    /**
     * This method defines the password encoder bean.
     * It uses BCrypt hashing algorithm to securely encode passwords.
     * All passwords stored in the database will be BCrypt encoded.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This method configures the security filter chain for the application.
     * It defines:
     * - Which URLs are public
     * - Which URLs require authentication
     * - Login configuration
     * - Logout configuration
     * - JWT filter placement
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())  // Disables CSRF protection (useful for APIs)

            .authorizeHttpRequests(auth -> auth

                // Publicly accessible URLs (no authentication required)
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/register",
                        "/login",
                        "/forgot-password",
                        "/verify-color",
                        "/reset-password"
                ).permitAll()

                // API endpoints require authentication
                .requestMatchers("/api/**").authenticated()

                // Any other request must be authenticated
                .anyRequest().authenticated()
            )

            // Configures form-based login
            .formLogin(form -> form
                    .loginPage("/login")                      // Custom login page
                    .loginProcessingUrl("/login")             // URL where login form submits
                    .defaultSuccessUrl("/dashboard", true)    // Redirect after successful login
                    .failureUrl("/login?error=true")          // Redirect if login fails
                    .permitAll()
            )

            // Configures logout behavior
            .logout(logout -> logout
                    .logoutUrl("/logout")                     // Logout URL
                    .logoutSuccessUrl("/login?logout")        // Redirect after logout
            )

            // Adds JWT filter before Spring's default authentication filter
            // This ensures JWT token is validated before authentication happens
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * This method configures the DAO authentication provider.
     * It connects Spring Security with:
     * - CustomUserDetailsService (loads user from database)
     * - PasswordEncoder (verifies encoded passwords)
     *
     * It is responsible for authenticating users using database credentials.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    /**
     * This method exposes AuthenticationManager as a bean.
     * AuthenticationManager is responsible for processing authentication requests.
     * It is typically used for:
     * - Manual authentication
     * - JWT authentication flow
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}