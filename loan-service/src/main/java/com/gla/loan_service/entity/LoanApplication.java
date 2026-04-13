package com.gla.loan_service.entity;

import com.gla.loan_service.enums.AccountType;
import com.gla.loan_service.enums.LoanStatus;
import com.gla.loan_service.enums.LoanType;
import com.gla.loan_service.enums.RejectionReason;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    // ✅ LOAN DETAILS
    private Double amount;
    private Integer tenure;
    private Integer creditScore;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Column(length = 1000)
    private String loanPurposeDescription;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    // ✅ BANK ACCOUNT INFO
    private String bankName;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private String ifscCode;

    // ✅ FINANCIAL SNAPSHOT AT APPLICATION TIME
    private Double monthlyIncome;
    private Double requestedEMI;

    // =============================
    // 🔥 APPROVAL FIELDS
    // =============================
    private Double approvedAmount;
    private Double interestRate;
    private Double finalEMI;
    private Double processingFee;

    // Branch visit
    private LocalDate branchVisitDate;
    private String branchVisitTimeSlot;
    private String branchName;
    private String branchAddress;
    private String branchContactNumber;

    @Column(length = 1000)
    private String approvalRemarks;

    // =============================
    // 🔥 REJECTION FIELDS
    // =============================
    @Enumerated(EnumType.STRING)
    private RejectionReason rejectionReason;

    @Column(length = 2000)
    private String rejectionMessage;

    private LocalDate reapplyEligibleDate;

    // =============================
    // 🔥 TIMESTAMPS
    // =============================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = LoanStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}