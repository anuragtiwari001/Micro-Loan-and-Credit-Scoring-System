package com.gla.credit_scoring_service.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Full ML model response from ml-model-service.
 *
 * ADMIN sees all fields.
 * USER sees only normalizedCreditScore (via UserScoreResponse wrapper).
 *
 * SCORE BANDS (matches CreditScoreHistory and loan-service getCreditRating()):
 *   800 – 850  → EXCELLENT  → instant approval
 *   740 – 799  → VERY_GOOD  → easy approval
 *   670 – 739  → GOOD       → standard rates
 *   580 – 669  → FAIR       → conditional / higher rates
 *   300 – 579  → POOR       → likely rejected
 *
 * riskTier  → mirrors score bands: EXCELLENT / VERY_GOOD / GOOD / FAIR / POOR
 * decision  → ML recommendation: APPROVE / REVIEW / REJECT
 *             Note: admin makes the final call; this is advisory only.
 */
@Data
public class PredictionResponse {

    /** Raw model output probability (0.0 – 1.0) before normalization */
    private Double rawProbability;

    /** Normalized score on 300–850 FICO-style scale */
    private Integer normalizedCreditScore;

    /** EXCELLENT | VERY_GOOD | GOOD | FAIR | POOR */
    private String riskTier;

    /** ML advisory: APPROVE | REVIEW | REJECT */
    private String decision;

    /** Model version string e.g. "ensemble-v2.1" or "fallback-rule-based" */
    private String modelVersion;

    /** Model name e.g. "EnsembleModel" or "RuleBasedFallback" */
    private String modelUsed;

    /** Ensemble confidence score (0.0 – 1.0) */
    private Double confidence;

    /**
     * Feature importance map from RandomForest.
     * Keys: feature names, Values: importance scores.
     * Example: { "debtToIncomeRatio": 0.28, "paymentRegularity": 0.22, ... }
     */
    private Map<String, Double> featureImportance;

    /**
     * Human-readable reason codes explaining the score.
     * Example: ["HIGH_DEBT_TO_INCOME", "GOOD_PAYMENT_HISTORY", "STABLE_INCOME"]
     */
    private List<String> reasonCodes;
}