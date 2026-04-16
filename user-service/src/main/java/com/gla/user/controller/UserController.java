package com.gla.user.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.common_service.enums.Role;
import com.gla.user.dto.LoginRequest;
import com.gla.user.dto.LoginResponse;
import com.gla.user.entity.User;
import com.gla.user.repository.UserRepository;
import com.gla.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public user endpoints (no JWT required).
 *
 * POST /api/v1/users/register  → create USER account
 * POST /api/v1/users/login     → authenticate and get JWT + role
 * GET  /api/v1/users/email/{email} → fetch user by email (called by loan-service)
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository repo;

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new USER account.
     * Role is always forced to USER — callers cannot self-assign ADMIN.
     *
     * Request:  { "email": "...", "password": "..." }
     * Response: 201 { success:true, message:"...", data:"User registered successfully" }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody User user) {
        user.setRole(Role.USER);   // always force USER role on self-registration
        String result = service.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Registration successful", result));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a JWT + role.
     *
     * Request:  { "email": "...", "password": "..." }
     * Response: 200 { success:true, message:"Login successful",
     *                 data: { "token": "eyJ...", "role": "USER" } }
     *
     * FRONTEND NOTE:
     *   Store token in memory or a secure httpOnly cookie.
     *   Use `data.role` to navigate to /user/dashboard or /admin/dashboard.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse loginResponse = service.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", loginResponse));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal endpoint — called by loan-service to resolve email → User object
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the User entity (including id) for a given email.
     * Used by loan-service to get the userId for the credit admin score call.
     *
     * This endpoint is protected by JWT — only services with a valid token
     * (forwarded from the original request) can call it.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(
            @PathVariable String email) {

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + email));

        return ResponseEntity.ok(
                ApiResponse.success("User found", user));
    }
}

//package com.gla.user.controller;
//
//import com.gla.common_service.enums.Role;
//import com.gla.user.dto.LoginRequest;
//import com.gla.user.entity.User;
//import com.gla.user.repository.UserRepository;
//import com.gla.user.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/users")
//public class UserController {
//
//    @Autowired
//    private UserService service;
//
//    @Autowired
//    private UserRepository repo;
//
//    @PostMapping("/register")
//    public String register(@RequestBody User user) {
//
//        // 🔥 FORCE ROLE
//        user.setRole(Role.USER);
//
//        return service.register(user);
//    }
//
//    // ✅ LOGIN
//    @PostMapping("/login")
//    public String login(@RequestBody LoginRequest request) {
//        return service.login(request);
//    }
//
//    // 🔥 NEW API (REQUIRED FOR DASHBOARD)
//    @GetMapping("/email/{email}")
//    public User getUserByEmail(@PathVariable String email) {
//
//        return repo.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }
//
//}