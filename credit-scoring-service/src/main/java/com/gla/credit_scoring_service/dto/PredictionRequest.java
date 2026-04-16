package com.gla.credit_scoring_service.dto;

import lombok.Data;

/**
 * Request body sent to ml-model-service POST /api/v1/ml/predict.
 *
 * RAW FIELDS (from UserFinancialData):
 *   monthlyIncome, monthlyExpenses, existingEMIs,
 *   numDependents, averageBalance, missedPaymentsCount,
 *   totalTransactions, employmentLengthMonths
 *
 * DERIVED FEATURES (computed by CreditScoreService.buildPredictionRequest):
 *   debtToIncomeRatio       = (expenses + existingEMIs) / income
 *   incomeConsistencyScore  = 1 - CV(salaryHistory)         [0.0 – 1.0]
 *   spendingVolatilityIndex = CV(monthlyBalanceHistory)     [0.0 – 1.0]
 *   savingsRate             = (income - expenses) / income  [0.0 – 1.0]
 *   paymentRegularity       = 1 - (missedPayments / totalTx)[0.0 – 1.0]
 *
 * ml-model-service uses EnsembleScoringService:
 *   LogisticRegression (35%) + RandomForest (65%)
 * If models are not loaded, RuleBasedFallbackService kicks in.
 */
@Data
public class PredictionRequest {

    // ── Raw inputs ────────────────────────────────────────────────────────────
    private Double  monthlyIncome;
    private Double  monthlyExpenses;
    private Double  existingEMIs;
    private Integer numDependents;
    private Double  averageBalance;
    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;

    // ── Derived features (computed before sending) ────────────────────────────
    private Double debtToIncomeRatio;
    private Double incomeConsistencyScore;
    private Double spendingVolatilityIndex;
    private Double savingsRate;
    private Double paymentRegularity;
}

