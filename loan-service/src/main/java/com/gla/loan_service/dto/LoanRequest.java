package com.gla.loan_service.dto;

import lombok.Data;

@Data
public class LoanRequest {

    private Double amount;
    private Integer tenure;
    private String loanType;
    private String loanPurposeDescription;

    // ✅ BANK ACCOUNT INFO
    private String bankName;
    private String accountNumber;
    private String accountType;
    private String ifscCode;

    // ✅ FINANCIAL INFO
    private Double monthlyIncome;
    private Double requestedEMI;
}