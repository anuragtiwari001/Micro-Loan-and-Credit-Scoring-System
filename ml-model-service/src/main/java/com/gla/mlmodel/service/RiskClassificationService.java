package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.PredictionRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskClassificationService {

    public int normalizeToCreditScore(double probability) {
        // Normalization formula: creditScore = 300 + (repaymentProbability * 550)
        int score = (int) Math.round(300 + (probability * 550));
        return Math.max(300, Math.min(850, score));
    }

    public String determineRiskTier(int creditScore) {
        if (creditScore >= 750) return "LOW_RISK";
        if (creditScore >= 650) return "MODERATE_RISK";
        if (creditScore >= 550) return "HIGH_RISK";
        return "VERY_HIGH_RISK";
    }

    public String determineDecision(String riskTier) {
        return switch (riskTier) {
            case "LOW_RISK" -> "AUTO_APPROVE";
            case "MODERATE_RISK" -> "APPROVE_WITH_CONDITIONS";
            case "HIGH_RISK" -> "MANUAL_REVIEW_REQUIRED";
            default -> "DECLINE";
        };
    }

    public List<String> generateReasonCodes(PredictionRequest request) {
        List<String> codes = new ArrayList<>();

        if (request.getDebtToIncomeRatio() != null) {
            if (request.getDebtToIncomeRatio() < 0.3) codes.add("LOW_DTI");
            else if (request.getDebtToIncomeRatio() > 0.6) codes.add("HIGH_DTI");
        }

        if (request.getIncomeConsistencyScore() != null) {
            if (request.getIncomeConsistencyScore() > 0.8) codes.add("STABLE_INCOME");
            else if (request.getIncomeConsistencyScore() < 0.4) codes.add("UNSTABLE_INCOME");
        }

        if (request.getSpendingVolatilityIndex() != null) {
            if (request.getSpendingVolatilityIndex() > 0.6) codes.add("HIGH_SPENDING_VOLATILITY");
        }

        if (request.getSavingsRate() != null) {
            if (request.getSavingsRate() > 20) codes.add("STRONG_SAVINGS_RATE");
            else if (request.getSavingsRate() < 5) codes.add("WEAK_SAVINGS_RATE");
        }

        if (request.getPaymentRegularity() != null) {
            if (request.getPaymentRegularity() > 0.95) codes.add("EXCELLENT_PAYMENT_REGULARITY");
            else if (request.getPaymentRegularity() < 0.70) codes.add("POOR_PAYMENT_REGULARITY");
        }

        return codes;
    }
}
