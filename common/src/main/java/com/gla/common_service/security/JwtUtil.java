package com.gla.common_service.security;

import com.gla.common_service.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import com.gla.common_service.enums.Role;

public class JwtUtil {

    private static final String SECRET = "gla_microservices_secret_key_2026_secure";
    private static final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 🔥 GENERATE TOKEN


    public static String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔥 EXTRACT CLAIMS
    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }

    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public static String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public static boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}