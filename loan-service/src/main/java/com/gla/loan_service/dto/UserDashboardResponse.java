package com.gla.loan_service.dto;

import com.gla.loan_service.enums.LoanStatus;
import com.gla.loan_service.enums.RejectionReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboardResponse {

    // ✅ LOAN BASICS
    private Long loanId;
    private Double requestedAmount;
    private Integer tenure;
    private String loanType;
    private String loanPurpose;
    private LoanStatus status;
    private LocalDateTime appliedAt;

    // ✅ CREDIT INFO
    private Integer creditScore;
    private String creditRating;         // EXCELLENT / GOOD / FAIR / POOR

    // ✅ SHOWN ON APPROVAL
    private Double approvedAmount;
    private Double interestRate;
    private Double finalEMI;
    private Double processingFee;
    private Double totalRepaymentAmount; // approvedAmount + total interest

    // Branch visit info
    private LocalDate branchVisitDate;
    private String branchVisitTimeSlot;
    private String branchName;
    private String branchAddress;
    private String branchContactNumber;
    private String approvalRemarks;

    // ✅ SHOWN ON REJECTION
    private RejectionReason rejectionReason;
    private String rejectionMessage;
    private LocalDate reapplyEligibleDate;

    // ✅ NEXT ACTION MESSAGE (shown prominently on dashboard)
    private String nextAction;
    private String nextActionDescription;
}