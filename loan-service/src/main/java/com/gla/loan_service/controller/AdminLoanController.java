package com.gla.loan_service.controller;

import com.gla.loan_service.dto.ApprovalRequest;
import com.gla.loan_service.dto.RejectionRequest;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.repository.LoanRepository;
import com.gla.loan_service.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/loans")
public class AdminLoanController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanService service;

    // ✅ ADMIN VALIDATION
    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied");
        }
    }

    // ✅ ALL LOANS
    @GetMapping
    public List<LoanApplication> getAll(HttpServletRequest request) {
        validateAdmin(request);
        return loanRepository.findAll();
    }

    // ✅ FULL DETAILS (Loan + Docs)
    @GetMapping("/details/{id}")
    public Object getDetails(@PathVariable Long id,
                             HttpServletRequest request) {
        validateAdmin(request);
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }
        return service.getFullLoanDetails(id, token);
    }

    // ✅ ADMIN DASHBOARD (Loan + Docs + Credit Score + Recommendation)
    @GetMapping("/dashboard/{id}")
    public Object getDashboard(@PathVariable Long id,
                               HttpServletRequest request) {
        validateAdmin(request);
        String token = request.getHeader("Authorization");
        return service.getAdminDashboard(id, token);
    }

    // ====================================================
    // 🔥 APPROVE — Full offer details + branch scheduling
    // ====================================================
    @PutMapping("/approve/{id}")
    public LoanApplication approve(
            @PathVariable Long id,
            @RequestBody ApprovalRequest approvalRequest,
            HttpServletRequest request) {

        validateAdmin(request);
        return service.approveLoan(id, approvalRequest);
    }

    // ====================================================
    // 🔥 REJECT — With reason + message + reapply date
    // ====================================================
    @PutMapping("/reject/{id}")
    public LoanApplication reject(
            @PathVariable Long id,
            @RequestBody RejectionRequest rejectionRequest,
            HttpServletRequest request) {

        validateAdmin(request);
        return service.rejectLoan(id, rejectionRequest);
    }
}