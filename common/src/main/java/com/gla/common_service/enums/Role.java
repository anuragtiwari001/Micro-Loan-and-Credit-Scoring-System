package com.gla.common_service.enums;

/**
 * Shared role enum used by every service for JWT claims and Spring Security authorities.
 * Value stored in JWT: role.name() → "USER" or "ADMIN"
 * Spring authority: "ROLE_USER" or "ROLE_ADMIN" (prefix added by JwtFilter)
 */
public enum Role {
    USER,
    ADMIN
}