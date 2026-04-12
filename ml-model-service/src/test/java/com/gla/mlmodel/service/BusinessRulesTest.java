package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.PredictionRequest;
import com.gla.mlmodel.exception.ModelServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRulesTest {

    private final FeatureEngineeringService featureEng = new FeatureEngineeringService();
    private final RiskClassificationService riskService = new RiskClassificationService();
    
    @Test
    void testFeatureEngineeringComputesMissingValues() {
        PredictionRequest req = PredictionRequest.builder()
                .monthlyIncome(5000.0)
                .monthlyExpenses(2000.0)
                .existingEMIs(1000.0)
                .build();
                
        featureEng.processAndValidateFeatures(req);
        
        assertEquals(1000.0 / 5000.0, req.getDebtToIncomeRatio());
        assertEquals(((5000.0 - 2000.0) / 5000.0) * 100, req.getSavingsRate());
        assertNotNull(req.getIncomeConsistencyScore());
        assertNotNull(req.getSpendingVolatilityIndex());
        assertNotNull(req.getPaymentRegularity());
    }
    
    @Test
    void testRuleBasedFallbackCalculatesCorrectProbability() {
        RuleBasedFallbackService fallback = new RuleBasedFallbackService();
        ReflectionTestUtils.setField(fallback, "wDti", 0.25);
        ReflectionTestUtils.setField(fallback, "wIncCons", 0.22);
        ReflectionTestUtils.setField(fallback, "wSpendVol", 0.18);
        ReflectionTestUtils.setField(fallback, "wSavings", 0.15);
        ReflectionTestUtils.setField(fallback, "wPaymReg", 0.20);
        
        PredictionRequest req = PredictionRequest.builder()
                .debtToIncomeRatio(0.4) // excellent (0.8 score)
                .incomeConsistencyScore(0.9) // 0.9 score
                .spendingVolatilityIndex(0.2) // 0.8 score
                .savingsRate(30.0) // 0.6 score
                .paymentRegularity(1.0) // 1.0 score
                .build();
                
        double prob = fallback.calculateFallbackProbability(req);
        assertTrue(prob > 0.70 && prob <= 1.0);
    }
    
    @Test
    void testRiskClassification() {
        int score = riskService.normalizeToCreditScore(0.9); // 300 + 0.9*550 = 795
        assertEquals(795, score);
        assertEquals("LOW_RISK", riskService.determineRiskTier(score));
        assertEquals("AUTO_APPROVE", riskService.determineDecision("LOW_RISK"));
        
        PredictionRequest req = PredictionRequest.builder()
                .debtToIncomeRatio(0.2)
                .paymentRegularity(0.98)
                .build();
        List<String> reasons = riskService.generateReasonCodes(req);
        assertTrue(reasons.contains("LOW_DTI"));
        assertTrue(reasons.contains("EXCELLENT_PAYMENT_REGULARITY"));
    }
}
