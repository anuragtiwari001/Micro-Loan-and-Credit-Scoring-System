package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.PredictionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RuleBasedFallbackService {

    @Value("${app.business.fallback-weights.debtToIncomeRatio}")
    private double wDti;

    @Value("${app.business.fallback-weights.incomeConsistencyScore}")
    private double wIncCons;

    @Value("${app.business.fallback-weights.spendingVolatilityIndex}")
    private double wSpendVol;

    @Value("${app.business.fallback-weights.savingsRate}")
    private double wSavings;

    @Value("${app.business.fallback-weights.paymentRegularity}")
    private double wPaymReg;

    public double calculateFallbackProbability(PredictionRequest request) {
        // DTI: 0 is excellent, 5 is terrible. Map to 1.0 -> 0.0 contribution.
        double dtiScore = Math.max(0.0, 1.0 - (request.getDebtToIncomeRatio() / 2.0)); // Cap at DTI 2.0 for scoring
        
        // Income Consistency: assume 0.0 to 1.0, higher is better
        double incScore = Math.max(0.0, Math.min(1.0, request.getIncomeConsistencyScore()));
        
        // Spending Volatility: assume 0.0 to 1.0, lower is better
        double spendScore = Math.max(0.0, 1.0 - request.getSpendingVolatilityIndex());
        
        // Savings Rate: -100 to 100. Map > 0 appropriately. 50% = 1.0
        double savScore = Math.max(0.0, Math.min(1.0, request.getSavingsRate() / 50.0));
        
        // Payment Regularity: 0.0 to 1.0, higher is better
        double regScore = Math.max(0.0, Math.min(1.0, request.getPaymentRegularity()));

        double totalScore = (dtiScore * wDti) +
                            (incScore * wIncCons) +
                            (spendScore * wSpendVol) +
                            (savScore * wSavings) +
                            (regScore * wPaymReg);

        // Normalize sum of weights if not exactly 1.0
        double sumWeights = wDti + wIncCons + wSpendVol + wSavings + wPaymReg;
        
        return Math.min(1.0, Math.max(0.0, totalScore / sumWeights));
    }

    public Map<String, Double> getDefaultFeatureImportance() {
        Map<String, Double> importance = new HashMap<>();
        importance.put("debtToIncomeRatio", wDti);
        importance.put("incomeConsistencyScore", wIncCons);
        importance.put("spendingVolatilityIndex", wSpendVol);
        importance.put("savingsRate", wSavings);
        importance.put("paymentRegularity", wPaymReg);
        return importance;
    }
}
