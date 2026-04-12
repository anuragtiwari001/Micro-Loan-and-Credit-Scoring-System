package com.gla.user.controller;

import com.gla.common_service.enums.Role;
import com.gla.user.dto.LoginRequest;
import com.gla.user.entity.User;
import com.gla.user.repository.UserRepository;
import com.gla.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository repo;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        // 🔥 FORCE ROLE
        user.setRole(Role.USER);

        return service.register(user);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return service.login(request);
    }

    // 🔥 NEW API (REQUIRED FOR DASHBOARD)
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {

        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}