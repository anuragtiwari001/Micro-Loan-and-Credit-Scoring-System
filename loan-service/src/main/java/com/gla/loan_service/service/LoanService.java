package com.gla.loan_service.service;

import com.gla.loan_service.dto.*;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.enums.AccountType;
import com.gla.loan_service.enums.LoanStatus;
import com.gla.loan_service.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                                 String bankName,
                                 String accountNumber,
                                 String accountType,
                                 String ifscCode,
                                 Double monthlyIncome,
                                 Double requestedEMI,
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
        loan.setBankName(bankName);
        loan.setAccountNumber(accountNumber);
        loan.setIfscCode(ifscCode);
        loan.setMonthlyIncome(monthlyIncome);
        loan.setRequestedEMI(requestedEMI);

        if (accountType != null) {
            loan.setAccountType(AccountType.valueOf(accountType.toUpperCase()));
        }

        return repo.save(loan);
    }

    // =========================
    // 🔥 APPROVE LOAN (FULL)
    // =========================
    public LoanApplication approveLoan(Long loanId, ApprovalRequest approvalRequest) {

        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is already " + loan.getStatus());
        }

        // Set status
        loan.setStatus(LoanStatus.APPROVED);

        // Set approval details
        loan.setApprovedAmount(approvalRequest.getApprovedAmount());
        loan.setInterestRate(approvalRequest.getInterestRate());
        loan.setProcessingFee(approvalRequest.getProcessingFee());

        // 🔥 Calculate final EMI automatically
        // Formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        // where r = monthly interest rate, n = tenure in months
        double principal = approvalRequest.getApprovedAmount();
        double annualRate = approvalRequest.getInterestRate();
        double monthlyRate = annualRate / (12 * 100);
        int months = loan.getTenure();

        double emi;
        if (monthlyRate == 0) {
            emi = principal / months;
        } else {
            double power = Math.pow(1 + monthlyRate, months);
            emi = (principal * monthlyRate * power) / (power - 1);
        }
        loan.setFinalEMI(Math.round(emi * 100.0) / 100.0);

        // Branch visit details
        loan.setBranchVisitDate(approvalRequest.getBranchVisitDate());
        loan.setBranchVisitTimeSlot(approvalRequest.getBranchVisitTimeSlot());
        loan.setBranchName(approvalRequest.getBranchName());
        loan.setBranchAddress(approvalRequest.getBranchAddress());
        loan.setBranchContactNumber(approvalRequest.getBranchContactNumber());
        loan.setApprovalRemarks(approvalRequest.getApprovalRemarks());

        return repo.save(loan);
    }

    // =========================
    // 🔥 REJECT LOAN (FULL)
    // =========================
    public LoanApplication rejectLoan(Long loanId, RejectionRequest rejectionRequest) {

        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new RuntimeException("Loan is already " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(rejectionRequest.getRejectionReason());
        loan.setRejectionMessage(rejectionRequest.getRejectionMessage());
        loan.setReapplyEligibleDate(rejectionRequest.getReapplyEligibleDate());

        return repo.save(loan);
    }

    // =========================
    // 🔥 USER DASHBOARD
    // =========================
    public List<UserDashboardResponse> getUserDashboard(String email) {

        List<LoanApplication> loans = repo.findByEmail(email);

        if (loans.isEmpty()) {
            throw new RuntimeException("No loan applications found for user");
        }

        return loans.stream()
                .map(this::buildUserDashboard)
                .collect(Collectors.toList());
    }

    // 🔥 BUILD DASHBOARD RESPONSE
    private UserDashboardResponse buildUserDashboard(LoanApplication loan) {

        UserDashboardResponse dashboard = new UserDashboardResponse();

        // Basics
        dashboard.setLoanId(loan.getId());
        dashboard.setRequestedAmount(loan.getAmount());
        dashboard.setTenure(loan.getTenure());
        dashboard.setLoanType(loan.getLoanType().name());
        dashboard.setLoanPurpose(loan.getLoanPurposeDescription());
        dashboard.setStatus(loan.getStatus());
        dashboard.setAppliedAt(loan.getCreatedAt());
        dashboard.setCreditScore(loan.getCreditScore());
        dashboard.setCreditRating(getCreditRating(loan.getCreditScore()));

        // Status-based population
        switch (loan.getStatus()) {

            case PENDING -> {
                dashboard.setNextAction("WAIT_FOR_REVIEW");
                dashboard.setNextActionDescription(
                        "Your loan application is under review. " +
                                "Our team will process it within 3-5 business days."
                );
            }

            case APPROVED -> {
                // Offer details
                dashboard.setApprovedAmount(loan.getApprovedAmount());
                dashboard.setInterestRate(loan.getInterestRate());
                dashboard.setFinalEMI(loan.getFinalEMI());
                dashboard.setProcessingFee(loan.getProcessingFee());

                // Total repayment
                if (loan.getFinalEMI() != null && loan.getTenure() != null) {
                    dashboard.setTotalRepaymentAmount(
                            Math.round(loan.getFinalEMI() * loan.getTenure() * 100.0) / 100.0
                    );
                }

                // Branch visit
                dashboard.setBranchVisitDate(loan.getBranchVisitDate());
                dashboard.setBranchVisitTimeSlot(loan.getBranchVisitTimeSlot());
                dashboard.setBranchName(loan.getBranchName());
                dashboard.setBranchAddress(loan.getBranchAddress());
                dashboard.setBranchContactNumber(loan.getBranchContactNumber());
                dashboard.setApprovalRemarks(loan.getApprovalRemarks());

                dashboard.setNextAction("VISIT_BRANCH");
                dashboard.setNextActionDescription(
                        "Congratulations! Your loan is approved. " +
                                "Please visit " + loan.getBranchName() +
                                " on " + loan.getBranchVisitDate() +
                                " between " + loan.getBranchVisitTimeSlot() +
                                " with your original documents for final processing."
                );
            }

            case REJECTED -> {
                dashboard.setRejectionReason(loan.getRejectionReason());
                dashboard.setRejectionMessage(loan.getRejectionMessage());
                dashboard.setReapplyEligibleDate(loan.getReapplyEligibleDate());

                dashboard.setNextAction("REAPPLY_LATER");
                dashboard.setNextActionDescription(
                        "Your loan application was not approved. " +
                                "Reason: " + formatRejectionReason(loan.getRejectionReason()) +
                                ". You can reapply after " + loan.getReapplyEligibleDate() + "."
                );
            }
        }

        return dashboard;
    }

    // =========================
    // HELPERS
    // =========================
    private String getCreditRating(Integer score) {
        if (score == null) return "UNKNOWN";
        if (score >= 800) return "EXCELLENT";
        if (score >= 740) return "VERY_GOOD";
        if (score >= 670) return "GOOD";
        if (score >= 580) return "FAIR";
        return "POOR";
    }

    private String formatRejectionReason(
            com.gla.loan_service.enums.RejectionReason reason) {
        if (reason == null) return "Not specified";
        return reason.name().replace("_", " ").toLowerCase();
    }

    // =========================
    // GET ALL LOANS
    // =========================
    public List<LoanApplication> getAllLoans() {
        return repo.findAll();
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
    // CREDIT SERVICE ADMIN
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
        return Map.of("loan", loan, "documents", docs);
    }

    // =========================
    // GET USER ID BY EMAIL
    // =========================
    public Long getUserIdByEmail(String email, String token) {
        String url = "http://localhost:8081/api/v1/users/email/" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("User not found for email: " + email);
        }

        Object idValue = response.getBody().get("id");
        if (idValue == null) {
            throw new RuntimeException("User ID missing in response");
        }

        return ((Number) idValue).longValue();
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================
    public Object getAdminDashboard(Long loanId, String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Token");
        }

        LoanApplication loan = repo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Object documents = getUserDocuments(loan.getEmail(), token);
        Long userId = getUserIdByEmail(loan.getEmail(), token);
        Object creditDetails = getCreditScoreFromAdmin(userId, token);

        Integer score = loan.getCreditScore();
        String recommendation;
        if (score >= 750) recommendation = "APPROVE";
        else if (score >= 600) recommendation = "REVIEW";
        else recommendation = "REJECT";

        return new AdminDashboardResponse(loan, documents, creditDetails, recommendation);
    }
}