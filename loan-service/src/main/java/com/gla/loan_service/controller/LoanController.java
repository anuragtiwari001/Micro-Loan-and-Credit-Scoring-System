package com.gla.loan_service.controller;

import com.gla.loan_service.dto.LoanRequest;
import com.gla.loan_service.dto.UserDashboardResponse;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan")
public class LoanController {

    @Autowired
    private LoanService service;

    // ✅ APPLY FOR LOAN
    @PostMapping("/apply")
    public LoanApplication apply(@RequestBody LoanRequest requestBody,
                                 HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        String token = request.getHeader("Authorization");

        return service.apply(
                email,
                requestBody.getAmount(),
                requestBody.getTenure(),
                requestBody.getLoanType(),
                requestBody.getLoanPurposeDescription(),
                requestBody.getBankName(),
                requestBody.getAccountNumber(),
                requestBody.getAccountType(),
                requestBody.getIfscCode(),
                requestBody.getMonthlyIncome(),
                requestBody.getRequestedEMI(),
                token
        );
    }

    // 🔥 USER DASHBOARD — all loan applications + status + next action
    @GetMapping("/dashboard")
    public List<UserDashboardResponse> getUserDashboard(HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        return service.getUserDashboard(email);
    }
}