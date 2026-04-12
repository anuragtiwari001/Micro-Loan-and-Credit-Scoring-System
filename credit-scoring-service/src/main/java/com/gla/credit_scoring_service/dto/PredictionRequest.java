package com.gla.credit_scoring_service.dto;

import lombok.Data;

@Data
public class PredictionRequest {

    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Double existingEMIs;
    private Integer numDependents;
    private Double averageBalance;
    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;

    private Double debtToIncomeRatio;
    private Double incomeConsistencyScore;
    private Double spendingVolatilityIndex;
    private Double savingsRate;
    private Double paymentRegularity;
}