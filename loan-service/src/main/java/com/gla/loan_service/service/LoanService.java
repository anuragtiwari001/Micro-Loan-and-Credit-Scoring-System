package com.gla.loan_service.service;

import com.gla.loan_service.dto.AdminDashboardResponse;
import com.gla.loan_service.dto.CreditResponse;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.enums.LoanStatus;
import com.gla.loan_service.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    @Autowired
    private LoanRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    // =========================
    // APPLY LOAN
    // =========================
    public LoanApplication apply(String email,
                                 Double amount,
                                 Integer tenure,
                                 String loanType,
                                 String purpose,
                                 String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<CreditResponse> response =
                restTemplate.exchange(
                        "http://localhost:8083/api/v1/credit/score",
                        HttpMethod.GET,
                        entity,
                        CreditResponse.class
                );

        if (response.getBody() == null) {
            throw new RuntimeException("Credit service returned null");
        }

        Integer score = response.getBody().getNormalizedCreditScore();

        LoanApplication loan = new LoanApplication();
        loan.setEmail(email);
        loan.setAmount(amount);
        loan.setTenure(tenure);
        loan.setCreditScore(score);
        loan.setLoanPurposeDescription(purpose);
        loan.setLoanType(
                com.gla.loan_service.enums.LoanType.valueOf(loanType.toUpperCase())
        );

        return repo.save(loan);
    }

    // =========================
    // GET ALL LOANS
    // =========================
    public List<LoanApplication> getAllLoans() {
        return repo.findAll();
    }

    // =========================
    // APPROVE
    // =========================
    public String approveLoan(Long id) {
        LoanApplication loan = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.APPROVED);
        repo.save(loan);

        return "Loan Approved";
    }

    // =========================
    // REJECT
    // =========================
    public String rejectLoan(Long id) {
        LoanApplication loan = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        repo.save(loan);

        return "Loan Rejected";
    }

    // =========================
    // DOCUMENT SERVICE CALL
    // =========================
    public Object getUserDocuments(String email, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "http://localhost:8086/api/v1/docs/admin/" + email,
                HttpMethod.GET,
                entity,
                Object.class
        );

        return response.getBody();
    }

    // =========================
    // CREDIT SERVICE (ADMIN API)
    // =========================
    public Object getCreditScoreFromAdmin(Long userId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        Map<String, Long> body = Map.of("userId", userId);

        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "http://localhost:8083/api/v1/credit/admin/score",
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }

    // =========================
    // FULL LOAN DETAILS
    // =========================
    public Object getFullLoanDetails(Long loanId, String token) {

        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Object docs = getUserDocuments(loan.getEmail(), token);

        return Map.of(
                "loan", loan,
                "documents", docs
        );
    }

    // =========================
    // 🔥 GET USER ID BY EMAIL  ← THIS WAS THE BROKEN METHOD
    // =========================
    public Long getUserIdByEmail(String email, String token) {

        String url = "http://localhost:8081/api/v1/users/email/" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ✅ FIX 1: Deserialize as Map, not Spring Security's User class
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("User not found for email: " + email);
        }

        // ✅ FIX 2: Extract "id" from the map — NOT .getClass()
        Object idValue = response.getBody().get("id");

        if (idValue == null) {
            throw new RuntimeException("User ID missing in response for email: " + email);
        }

        // JSON numbers deserialize as Integer by default — cast safely to Long
        return ((Number) idValue).longValue();
    }

    // =========================
    // 🔥 ADMIN DASHBOARD
    // =========================
    public Object getAdminDashboard(Long loanId, String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Token");
        }

        // 1. Loan
        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // 2. Documents
        Object documents = getUserDocuments(loan.getEmail(), token);

        // 3. Fetch userId from User Service ✅ (now works correctly)
        Long userId = getUserIdByEmail(loan.getEmail(), token);

        // 4. Credit Score via Admin API
        Object creditDetails = getCreditScoreFromAdmin(userId, token);

        // 5. Recommendation
        Integer score = loan.getCreditScore();
        String recommendation;

        if (score >= 750) {
            recommendation = "APPROVE";
        } else if (score >= 600) {
            recommendation = "REVIEW";
        } else {
            recommendation = "REJECT";
        }

        return new AdminDashboardResponse(
                loan,
                documents,
                creditDetails,
                recommendation
        );
    }
}