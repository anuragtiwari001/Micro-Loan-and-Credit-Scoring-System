package com.gla.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO.
 *
 * WHY include `role` here:
 *   The frontend needs the role immediately after login to decide which
 *   dashboard to navigate to (/user/dashboard or /admin/dashboard).
 *   Including it here avoids a second API call and avoids the frontend
 *   having to decode the JWT (which would expose the JWT library dependency).
 *
 *   Response shape:
 *   {
 *     "success": true,
 *     "message": "Login successful",
 *     "data": {
 *       "token": "eyJhbGci...",
 *       "role": "USER"
 *     }
 *   }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    /** JWT string — frontend stores this and sends as Authorization: Bearer <token> */
    private String token;

    /** "USER" or "ADMIN" — use for immediate frontend routing */
    private String role;
}