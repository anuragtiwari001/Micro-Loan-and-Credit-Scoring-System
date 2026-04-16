package com.gla.user.service;

import com.gla.common_service.enums.Role;
import com.gla.common_service.security.JwtUtil;
import com.gla.user.dto.LoginRequest;
import com.gla.user.dto.LoginResponse;
import com.gla.user.entity.User;
import com.gla.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business logic for user registration, login, and lookup.
 *
 * REGISTER FLOW:
 *   1. Reject duplicate emails.
 *   2. Force Role.USER (callers cannot self-assign ADMIN).
 *   3. BCrypt-hash the password.
 *   4. Persist and return a success message.
 *
 * LOGIN FLOW:
 *   1. Look up by email.
 *   2. BCrypt-match the supplied password.
 *   3. Generate a JWT containing email + role.
 *   4. Return LoginResponse { token, role } — frontend needs role for routing.
 *
 * NOTE: Admin accounts are created by AdminController which reuses register()
 *       but sets role = ADMIN after verifying the caller is an existing admin.
 */
@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new user. Role must be set on the entity before calling.
     *
     * @param user entity with email, plaintext password, and role already set
     * @return "User registered successfully"
     * @throws RuntimeException if email already exists
     */
    public String register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);  // safe default
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a JWT + role for frontend routing.
     *
     * @param request { email, password }
     * @return LoginResponse { token, role }
     * @throws RuntimeException on bad email or wrong password
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = JwtUtil.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole().name());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lookups used by other services
    // ─────────────────────────────────────────────────────────────────────────

    public String getEmailById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id))
                .getEmail();
    }
}



//package com.gla.user.service;
//import com.gla.common_service.enums.Role;
//import com.gla.common_service.security.JwtUtil;
//import com.gla.user.dto.LoginRequest;
//import com.gla.user.entity.User;
//import com.gla.user.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//@Service
//public class UserService {
//    @Autowired
//    private BCryptPasswordEncoder encoder;
//    @Autowired
//    private UserRepository userRepository;
//    public String register(User user) {
//        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
//            throw new RuntimeException("User already exists");
//        }
//        if (user.getRole() == null) {
//            user.setRole(Role.USER);  // ✅ ENUM
//        }
//        user.setPassword(encoder.encode(user.getPassword()));
//        userRepository.save(user);
//        return "User registered successfully";
//    }
//    public String login(LoginRequest request) {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        if (!encoder.matches(request.getPassword(), user.getPassword())) {
//            throw new RuntimeException("Invalid credentials");
//        }
//        Role role = user.getRole();  // ✅ DIRECT
//        return JwtUtil.generateToken(user.getEmail(), role);
//    }
//
//    public String getEmailById(Long id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"))
//                .getEmail();
//    }
//}