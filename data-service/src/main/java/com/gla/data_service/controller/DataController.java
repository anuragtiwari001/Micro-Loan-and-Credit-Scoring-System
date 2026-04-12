package com.gla.data_service.controller;

import com.gla.data_service.entity.UserFinancialData;
import com.gla.data_service.service.UserFinancialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {

    @Autowired
    private UserFinancialService service;

    // ✅ SAVE (USER only)
    @PostMapping("/submit")
    public UserFinancialData save(@RequestBody UserFinancialData data,
                                  HttpServletRequest request) {

        String emailFromToken = (String) request.getAttribute("email");

        // 🔥 enforce user identity
        data.setEmail(emailFromToken);

        return service.save(data);
    }

    // ✅ UPDATE (USER only)
    @PutMapping("/update")
    public UserFinancialData update(@RequestBody UserFinancialData data,
                                    HttpServletRequest request) {

        String emailFromToken = (String) request.getAttribute("email");

        data.setEmail(emailFromToken);

        return service.update(data);
    }

    // ✅ GET (USED BY CREDIT SERVICE + ADMIN)
    @GetMapping("/get")
    public UserFinancialData get(@RequestParam String email,
                                 HttpServletRequest request) {

        String tokenEmail = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");

        // 🔐 ROLE-BASED ACCESS CONTROL
        if ("USER".equalsIgnoreCase(role)) {
            // USER → can access only own data
            if (!email.equals(tokenEmail)) {
                throw new RuntimeException("Unauthorized access");
            }
        }

        // ADMIN → allowed for any email (no restriction)

        return service.getByEmail(email);
    }
}