package com.gla.loan_service.dto;

import lombok.Data;

@Data
public class LoanRequest {

    private Double amount;
    private Integer tenure;
    private String loanType;
    private String loanPurposeDescription;
}