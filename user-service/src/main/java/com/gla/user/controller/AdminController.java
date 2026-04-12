package com.gla.user.controller;

import com.gla.common_service.enums.Role;
import com.gla.user.entity.User;
import com.gla.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private UserService service;

    @PostMapping("/create")
    public String createAdmin(@RequestBody User user, HttpServletRequest request) {

        // 🔥 CHECK ROLE FROM JWT
        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: Only ADMIN can create admin");
        }

        user.setRole(Role.ADMIN);

        return service.register(user);
    }

    @GetMapping("/user/{id}")
    public String getUserEmailById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String role = (String) request.getAttribute("role");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied");
        }

        return service.getEmailById(id);
    }
}