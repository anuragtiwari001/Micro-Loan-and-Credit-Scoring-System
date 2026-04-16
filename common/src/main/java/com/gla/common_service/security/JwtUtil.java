package com.gla.common_service.security;

import com.gla.common_service.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * Stateless JWT helper shared across ALL services.
 *
 * TOKEN STRUCTURE:
 *   sub   → email
 *   role  → "USER" | "ADMIN"
 *   iat   → issued-at
 *   exp   → issued-at + 10 hours
 *
 * SECURITY NOTE:
 *   The SECRET must be ≥ 32 characters for HS256 (256-bit key minimum).
 *   In production replace this with an environment variable:
 *     System.getenv("JWT_SECRET")
 *   Never commit the real secret to version control.
 */
public class JwtUtil {

    // ── CHANGE THIS IN PRODUCTION via env variable ────────────────────────
    private static final String SECRET =
            "gla_microservices_jwt_secret_key_2026_secure_min32chars!";

    /** Token lifetime: 10 hours */
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 10;

    private static final Key SIGNING_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    // ─────────────────────────────────────────────────────────────────────────
    // Generate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a signed JWT containing the caller's email and role.
     *
     * @param email user's email address (JWT subject)
     * @param role  USER or ADMIN
     * @return compact signed JWT string (no "Bearer " prefix)
     */
    public static String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())          // stored as "USER" / "ADMIN"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parse
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parses and validates the token, returning its claims body.
     * Strips "Bearer " prefix automatically if present.
     *
     * @throws JwtException if the token is invalid, expired, or tampered with
     */
    public static Claims extractClaims(String token) {
        String raw = token.startsWith("Bearer ")
                ? token.substring(7)
                : token;

        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(raw)
                .getBody();
    }

    /** @return email (JWT subject) */
    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /** @return "USER" or "ADMIN" */
    public static String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Quick validity check (signature + expiry).
     * Prefer catching {@link JwtException} in the filter for more detail.
     */
    public static boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

//package com.gla.common_service.security;
//
//import com.gla.common_service.enums.Role;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//
//import java.security.Key;
//import java.util.Date;
//import com.gla.common_service.enums.Role;
//
//public class JwtUtil {
//
//    private static final String SECRET = "gla_microservices_secret_key_2026_secure";
//    private static final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
//
//    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
//
//    // 🔥 GENERATE TOKEN
//
//
//    public static String generateToken(String email, Role role) {
//        return Jwts.builder()
//                .setSubject(email)
//                .claim("role", role.name())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // 🔥 EXTRACT CLAIMS
//    public static Claims extractClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token.replace("Bearer ", ""))
//                .getBody();
//    }
//
//    public static String extractEmail(String token) {
//        return extractClaims(token).getSubject();
//    }
//
//    public static String extractRole(String token) {
//        return extractClaims(token).get("role", String.class);
//    }
//
//    public static boolean isTokenValid(String token) {
//        try {
//            extractClaims(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}