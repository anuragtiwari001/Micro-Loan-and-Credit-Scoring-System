package com.gla.loan_service.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.loan_service.dto.AdminDashboardResponse;
import com.gla.loan_service.dto.ApprovalRequest;
import com.gla.loan_service.dto.RejectionRequest;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.repository.LoanRepository;
import com.gla.loan_service.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only loan management endpoints for loan-service (:8085).
 *
 * GET  /api/v1/admin/loans                     → all loan applications
 * GET  /api/v1/admin/loans/details/{id}         → loan + uploaded documents
 * GET  /api/v1/admin/loans/dashboard/{id}       → full analysis (loan + docs + live ML score)
 * PUT  /api/v1/admin/loans/approve/{id}         → approve with offer details
 * PUT  /api/v1/admin/loans/reject/{id}          → reject with structured reason
 *
 * Authentication: ADMIN JWT required (enforced by SecurityConfig .hasRole("ADMIN")).
 * The validateAdmin() helper provides a second layer of defense.
 */
@RestController
@RequestMapping("/api/v1/admin/loans")
public class AdminLoanController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanService service;

    // ─────────────────────────────────────────────────────────────────────────
    // List all loans
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns every loan application in the system.
     *
     * TODO (v2): Add pagination — GET /admin/loans?page=0&size=10
     *            Use loanRepository.findAll(Pageable) and return Page<LoanApplication>.
     *
     * Response: 200 { success:true, data: [ LoanApplication, ... ] }
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getAll(
            HttpServletRequest request) {
        validateAdmin(request);
        return ResponseEntity.ok(
                ApiResponse.success("All loans retrieved", loanRepository.findAll()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Loan + documents
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns full loan details plus all KYC documents for that applicant.
     * Documents are fetched from document-service via the forwarded token.
     *
     * Response: 200 { success:true, data: { loan: {...}, documents: [...] } }
     */
    @GetMapping("/details/{id}")
    public ResponseEntity<ApiResponse<Object>> getDetails(
            @PathVariable Long id,
            HttpServletRequest request) {
        validateAdmin(request);
        String token = requireToken(request);
        Object details = service.getFullLoanDetails(id, token);
        return ResponseEntity.ok(
                ApiResponse.success("Loan details retrieved", details));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Full admin dashboard (loan + docs + live ML score + recommendation)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The main admin review screen. Triggers a live ML model call to get
     * a fresh credit analysis for the applicant.
     *
     * Response: 200 { success:true, data: AdminDashboardResponse }
     */
    @GetMapping("/dashboard/{id}")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(
            @PathVariable Long id,
            HttpServletRequest request) {
        validateAdmin(request);
        String token = requireToken(request);
        AdminDashboardResponse dashboard = service.getAdminDashboard(id, token);
        return ResponseEntity.ok(
                ApiResponse.success("Admin dashboard loaded", dashboard));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Approve
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Approves a PENDING loan application with full offer details.
     * EMI is auto-calculated: P*r*(1+r)^n / ((1+r)^n - 1)
     *
     * Request body: { approvedAmount, interestRate, processingFee,
     *                 branchVisitDate, branchVisitTimeSlot, branchName,
     *                 branchAddress, branchContactNumber, approvalRemarks }
     *
     * Response: 200 { success:true, data: LoanApplication (status=APPROVED) }
     */
    @PutMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest approvalRequest,
            HttpServletRequest request) {
        validateAdmin(request);
        LoanApplication approved = service.approveLoan(id, approvalRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Loan approved successfully", approved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reject
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Rejects a PENDING loan application with a structured reason.
     *
     * Request body: { rejectionReason (enum), rejectionMessage, reapplyEligibleDate }
     *
     * Response: 200 { success:true, data: LoanApplication (status=REJECTED) }
     */
    @PutMapping("/reject/{id}")
    public ResponseEntity<ApiResponse<LoanApplication>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectionRequest rejectionRequest,
            HttpServletRequest request) {
        validateAdmin(request);
        LoanApplication rejected = service.rejectLoan(id, rejectionRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Loan rejected", rejected));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void validateAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN role required");
        }
    }

    private String requireToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or malformed Authorization header");
        }
        return token;
    }
}

//package com.gla.loan_service.controller;
//
//import com.gla.loan_service.dto.ApprovalRequest;
//import com.gla.loan_service.dto.RejectionRequest;
//import com.gla.loan_service.entity.LoanApplication;
//import com.gla.loan_service.repository.LoanRepository;
//import com.gla.loan_service.service.LoanService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/admin/loans")
//public class AdminLoanController {
//
//    @Autowired
//    private LoanRepository loanRepository;
//
//    @Autowired
//    private LoanService service;
//
//    // ✅ ADMIN VALIDATION
//    private void validateAdmin(HttpServletRequest request) {
//        String role = (String) request.getAttribute("role");
//        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied");
//        }
//    }
//
//    // ✅ ALL LOANS
//    @GetMapping
//    public List<LoanApplication> getAll(HttpServletRequest request) {
//        validateAdmin(request);
//        return loanRepository.findAll();
//    }
//
//    // ✅ FULL DETAILS (Loan + Docs)
//    @GetMapping("/details/{id}")
//    public Object getDetails(@PathVariable Long id,
//                             HttpServletRequest request) {
//        validateAdmin(request);
//        String token = request.getHeader("Authorization");
//        if (token == null || !token.startsWith("Bearer ")) {
//            throw new RuntimeException("Missing Authorization header");
//        }
//        return service.getFullLoanDetails(id, token);
//    }
//
//    // ✅ ADMIN DASHBOARD (Loan + Docs + Credit Score + Recommendation)
//    @GetMapping("/dashboard/{id}")
//    public Object getDashboard(@PathVariable Long id,
//                               HttpServletRequest request) {
//        validateAdmin(request);
//        String token = request.getHeader("Authorization");
//        return service.getAdminDashboard(id, token);
//    }
//
//    // ====================================================
//    // 🔥 APPROVE — Full offer details + branch scheduling
//    // ====================================================
//    @PutMapping("/approve/{id}")
//    public LoanApplication approve(
//            @PathVariable Long id,
//            @RequestBody ApprovalRequest approvalRequest,
//            HttpServletRequest request) {
//
//        validateAdmin(request);
//        return service.approveLoan(id, approvalRequest);
//    }
//
//    // ====================================================
//    // 🔥 REJECT — With reason + message + reapply date
//    // ====================================================
//    @PutMapping("/reject/{id}")
//    public LoanApplication reject(
//            @PathVariable Long id,
//            @RequestBody RejectionRequest rejectionRequest,
//            HttpServletRequest request) {
//
//        validateAdmin(request);
//        return service.rejectLoan(id, rejectionRequest);
//    }
//}