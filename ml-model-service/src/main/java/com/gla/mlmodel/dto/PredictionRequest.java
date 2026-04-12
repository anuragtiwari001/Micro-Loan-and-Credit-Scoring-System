package com.gla.mlmodel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    // Optional Raw fields
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Double existingEMIs;
    private Integer numDependents;
    private Double averageBalance;
    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;

    // Core engineered fields
    private Double debtToIncomeRatio;
    private Double incomeConsistencyScore;
    private Double spendingVolatilityIndex;
    private Double savingsRate;
    private Double paymentRegularity;
}
