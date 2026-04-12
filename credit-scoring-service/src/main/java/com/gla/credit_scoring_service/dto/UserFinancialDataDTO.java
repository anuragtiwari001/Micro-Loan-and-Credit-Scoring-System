package com.gla.credit_scoring_service.dto;

import lombok.Data;
@Data
public class UserFinancialDataDTO {

    private Double income;
    private Double expenses;
    private Double existingLoans;

    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;

    private Double averageBalance;

    private String monthlyBalanceHistory;
    private String salaryHistory;
}