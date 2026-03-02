package com.revpay.security;

import java.util.Date;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT Utility Class
 *
 * Purpose:
 * - Responsible for generating, extracting, and validating JWT tokens.
 * - Used in authentication flow for securing API endpoints.
 */
@Component
public class JwtUtil {

    // Secret key used to sign and verify JWT tokens
    // ⚠️ In production, this should be stored securely (e.g., environment variable)
    private final String SECRET = "revpay-secret-key-revpay-secret-key";

    /**
     * Generates JWT token for a given username.
     *
     * Function:
     * - Sets subject (username/email).
     * - Sets issued time (current time).
     * - Sets expiration time (1 day from now).
     * - Signs token using HMAC SHA key.
     *
     * @param username → email of authenticated user
     * @return Signed JWT token
     */
    public String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username) // Username stored inside token
                .setIssuedAt(new Date()) // Token creation time
                .setExpiration(
                        new Date(System.currentTimeMillis() + 86400000)
                ) // Expiry: 1 day (24 hours)
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes())
                ) // Sign using secret key
                .compact();
    }

    /**
     * Extracts username (subject) from JWT token.
     *
     * Function:
     * - Parses token using secret key.
     * - Retrieves subject (username/email).
     *
     * @param token → JWT token
     * @return Extracted username
     */
    public String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes()) // Verify using same secret
                .build()
                .parseClaimsJws(token) // Parse token
                .getBody()
                .getSubject(); // Get username
    }

    /**
     * Validates token against username.
     *
     * Function:
     * - Extracts username from token.
     * - Compares it with provided username.
     *
     * NOTE:
     * - This does NOT explicitly check expiration here.
     * - Expired tokens will automatically throw exception during parsing.
     *
     * @param token → JWT token
     * @param username → Expected username
     * @return true if token is valid
     */
    public boolean validateToken(String token, String username) {

        return extractUsername(token).equals(username);
    }
}