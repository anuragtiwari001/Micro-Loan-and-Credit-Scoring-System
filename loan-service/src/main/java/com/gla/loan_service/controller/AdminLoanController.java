package com.gla.loan_service.controller;

import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.enums.LoanStatus;
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

    // ✅ COMMON ADMIN VALIDATION
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

    // ✅ FULL DETAILS (Loan + Credit + Docs)
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

    // ✅ APPROVE
    @PutMapping("/approve/{id}")
    public String approve(@PathVariable Long id,
                          HttpServletRequest request) {

        validateAdmin(request);

        LoanApplication loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);

        return "Loan Approved";
    }

    // ✅ REJECT
    @PutMapping("/reject/{id}")
    public String reject(@PathVariable Long id,
                         HttpServletRequest request) {

        validateAdmin(request);

        LoanApplication loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        return "Loan Rejected";
    }
    @GetMapping("/dashboard/{id}")
    public Object getDashboard(@PathVariable Long id,
                               HttpServletRequest request) {

        String role = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access denied");
        }

        return service.getAdminDashboard(id, token);
    }
}

//package com.gla.loan_service.controller;
//
//import com.gla.loan_service.entity.LoanApplication;
//import com.gla.loan_service.enums.LoanStatus;
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
//    @Autowired
//    private LoanService service;
//
//    // ✅ ALL LOANS
//    @GetMapping
//    public List<LoanApplication> getAll(HttpServletRequest request) {
//
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied");
//        }
//
//        return loanRepository.findAll();
//    }
//    @GetMapping("/details/{id}")
//    public Object getDetails(@PathVariable Long id,
//                             HttpServletRequest request) {
//
//        String role = (String) request.getAttribute("role");
//        String token = request.getHeader("Authorization");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access denied");
//        }
//
//        return service.getFullLoanDetails(id, token);
//    }
//
//    // ✅ APPROVE
//    @PutMapping("/approve/{id}")
//    public String approve(@PathVariable Long id, HttpServletRequest request) {
//
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied");
//        }
//
//        LoanApplication loan = loanRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Loan not found"));
//
//        loan.setStatus(LoanStatus.APPROVED);
//        loanRepository.save(loan);
//
//        return "Loan Approved";
//    }
//
//    // ✅ REJECT
//    @PutMapping("/reject/{id}")
//    public String reject(@PathVariable Long id, HttpServletRequest request) {
//
//        String role = (String) request.getAttribute("role");
//
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied");
//        }
//
//        LoanApplication loan = loanRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Loan not found"));
//
//        loan.setStatus(LoanStatus.REJECTED);
//        loanRepository.save(loan);
//
//        return "Loan Rejected";
//    }
//}