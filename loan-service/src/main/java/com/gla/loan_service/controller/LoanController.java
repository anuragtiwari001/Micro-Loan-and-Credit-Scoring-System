package com.gla.loan_service.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.loan_service.dto.LoanRequest;
import com.gla.loan_service.dto.UserDashboardResponse;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing loan endpoints for loan-service (:8085).
 *
 * POST /api/v1/loan/apply      → apply for a loan (requires financial data + docs first)
 * GET  /api/v1/loan/dashboard  → get all loan applications for the authenticated user
 *
 * Authentication: valid JWT required (enforced by SecurityConfig).
 * Email is always taken from the JWT — never from the request body.
 */
@RestController
@RequestMapping("/api/v1/loan")
public class LoanController {

    @Autowired
    private LoanService service;

    // ─────────────────────────────────────────────────────────────────────────
    // Apply for a loan
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Submits a new loan application.
     *
     * Internally calls credit-scoring-service to fetch the applicant's current
     * credit score before saving the application.
     *
     * Request body:
     *   { amount, tenure, loanType, loanPurposeDescription,
     *     bankName, accountNumber, accountType, ifscCode,
     *     monthlyIncome, requestedEMI }
     *
     * Response: 201 { success:true, data: LoanApplication (status=PENDING) }
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanApplication>> apply(
            @Valid @RequestBody LoanRequest requestBody,
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        String token = request.getHeader("Authorization");

        LoanApplication loan = service.apply(email, requestBody, token);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Loan application submitted", loan));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User dashboard
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all loan applications for the authenticated user, with full
     * status details, approval offer (if approved), or rejection info.
     *
     * Response: 200 { success:true, data: [ UserDashboardResponse, ... ] }
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<UserDashboardResponse>>> getUserDashboard(
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        List<UserDashboardResponse> dashboard = service.getUserDashboard(email);
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard loaded", dashboard));
    }
}

//package com.gla.loan_service.controller;
//
//import com.gla.loan_service.dto.LoanRequest;
//import com.gla.loan_service.dto.UserDashboardResponse;
//import com.gla.loan_service.entity.LoanApplication;
//import com.gla.loan_service.service.LoanService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/loan")
//public class LoanController {
//
//    @Autowired
//    private LoanService service;
//
//    // ✅ APPLY FOR LOAN
//    @PostMapping("/apply")
//    public LoanApplication apply(@RequestBody LoanRequest requestBody,
//                                 HttpServletRequest request) {
//
//        String email = (String) request.getAttribute("email");
//        String token = request.getHeader("Authorization");
//
//        return service.apply(
//                email,
//                requestBody.getAmount(),
//                requestBody.getTenure(),
//                requestBody.getLoanType(),
//                requestBody.getLoanPurposeDescription(),
//                requestBody.getBankName(),
//                requestBody.getAccountNumber(),
//                requestBody.getAccountType(),
//                requestBody.getIfscCode(),
//                requestBody.getMonthlyIncome(),
//                requestBody.getRequestedEMI(),
//                token
//        );
//    }
//
//    // 🔥 USER DASHBOARD — all loan applications + status + next action
//    @GetMapping("/dashboard")
//    public List<UserDashboardResponse> getUserDashboard(HttpServletRequest request) {
//
//        String email = (String) request.getAttribute("email");
//        return service.getUserDashboard(email);
//    }
//}