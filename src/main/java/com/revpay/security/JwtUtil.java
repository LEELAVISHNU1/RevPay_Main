package com.revpay.security;

import java.util.Date;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String SECRET = "revpay-secret-key-revpay-secret-key";

    // Generates a JWT token with username and 1-day expiration
    public String generateToken(String username) {

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 86400000)
                )
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes())
                )
                .compact();
    }

    // Extracts username from the JWT token
    public String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Validates token by checking if it belongs to the given username
    public boolean validateToken(String token, String username) {

        return extractUsername(token).equals(username);
    }
}