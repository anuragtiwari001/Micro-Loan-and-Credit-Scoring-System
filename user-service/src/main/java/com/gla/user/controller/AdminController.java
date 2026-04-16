package com.gla.user.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.common_service.enums.Role;
import com.gla.user.entity.User;
import com.gla.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only user management endpoints.
 *
 * POST /api/v1/admin/create      → create a new ADMIN account
 * GET  /api/v1/admin/user/{id}   → get email by user ID
 *
 * All endpoints require a valid ADMIN JWT.
 * Role is verified from the JWT (request attribute set by JwtFilter).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private UserService service;

    // ─────────────────────────────────────────────────────────────────────────
    // Create admin account
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new ADMIN user. Only an existing ADMIN can do this.
     *
     * Request:  { "email": "...", "password": "..." }
     * Response: 201 { success:true, data:"User registered successfully" }
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createAdmin(
            @Valid @RequestBody User user,
            HttpServletRequest request) {

        validateAdmin(request);

        user.setRole(Role.ADMIN);
        String result = service.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Admin account created", result));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get email by ID
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<String>> getUserEmailById(
            @PathVariable Long id,
            HttpServletRequest request) {

        validateAdmin(request);
        String email = service.getEmailById(id);
        return ResponseEntity.ok(ApiResponse.success("Email found", email));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN role required");
        }
    }
}


//package com.gla.user.controller;
//
//import com.gla.common_service.enums.Role;
//import com.gla.user.entity.User;
//import com.gla.user.service.UserService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/admin")
//public class AdminController {
//
//    @Autowired
//    private UserService service;
//
//    @PostMapping("/create")
//    public String createAdmin(@RequestBody User user, HttpServletRequest request) {
//
//        // 🔥 CHECK ROLE FROM JWT
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied: Only ADMIN can create admin");
//        }
//
//        user.setRole(Role.ADMIN);
//
//        return service.register(user);
//    }
//
//    @GetMapping("/user/{id}")
//    public String getUserEmailById(
//            @PathVariable Long id,
//            HttpServletRequest request
//    ) {
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied");
//        }
//
//        return service.getEmailById(id);
//    }
//}