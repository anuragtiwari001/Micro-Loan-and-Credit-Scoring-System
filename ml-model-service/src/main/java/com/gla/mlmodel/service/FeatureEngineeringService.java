package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.PredictionRequest;
import com.gla.mlmodel.exception.ModelServiceException;
import org.springframework.stereotype.Service;

@Service
public class FeatureEngineeringService {

    public void processAndValidateFeatures(PredictionRequest request) {
        // Derive missing core features if raw features are present
        if (request.getDebtToIncomeRatio() == null && request.getExistingEMIs() != null && request.getMonthlyIncome() != null) {
            if (request.getMonthlyIncome() <= 0) {
                throw new ModelServiceException("monthlyIncome must be > 0 to compute debtToIncomeRatio.");
            }
            request.setDebtToIncomeRatio(request.getExistingEMIs() / request.getMonthlyIncome());
        }

        if (request.getSavingsRate() == null && request.getMonthlyIncome() != null && request.getMonthlyExpenses() != null) {
            if (request.getMonthlyIncome() <= 0) {
                throw new ModelServiceException("monthlyIncome must be > 0 to compute savingsRate.");
            }
            request.setSavingsRate(((request.getMonthlyIncome() - request.getMonthlyExpenses()) / request.getMonthlyIncome()) * 100.0);
        }

        if (request.getIncomeConsistencyScore() == null) {
            // Synthesize based on employment length if missing
            double empMonths = request.getEmploymentLengthMonths() != null ? request.getEmploymentLengthMonths() : 24.0;
            request.setIncomeConsistencyScore(Math.min(1.0, empMonths / 48.0));
        }

        if (request.getSpendingVolatilityIndex() == null) {
            // Synthesize based on total transactions if available
            double txns = request.getTotalTransactions() != null ? request.getTotalTransactions() : 50.0;
            request.setSpendingVolatilityIndex(Math.min(1.0, txns / 200.0));
        }

        if (request.getPaymentRegularity() == null) {
            // Synthesize based on missed payments
            int missed = request.getMissedPaymentsCount() != null ? request.getMissedPaymentsCount() : 0;
            request.setPaymentRegularity(Math.max(0.0, 1.0 - (missed * 0.15)));
        }

        // Validate final core features
        validateCoreFeatures(request);
    }

    private void validateCoreFeatures(PredictionRequest request) {
        if (request.getDebtToIncomeRatio() == null || Double.isNaN(request.getDebtToIncomeRatio())) {
            throw new ModelServiceException("debtToIncomeRatio is required and cannot be NaN.");
        }
        if (request.getDebtToIncomeRatio() < 0 || request.getDebtToIncomeRatio() > 5) {
            throw new ModelServiceException("debtToIncomeRatio must be between 0 and 5.");
        }

        if (request.getIncomeConsistencyScore() == null || Double.isNaN(request.getIncomeConsistencyScore())) {
            throw new ModelServiceException("incomeConsistencyScore is required.");
        }

        if (request.getSpendingVolatilityIndex() == null || Double.isNaN(request.getSpendingVolatilityIndex())) {
            throw new ModelServiceException("spendingVolatilityIndex is required.");
        }

        if (request.getSavingsRate() == null || Double.isNaN(request.getSavingsRate())) {
            throw new ModelServiceException("savingsRate is required.");
        }
        if (request.getSavingsRate() < -100 || request.getSavingsRate() > 100) {
            throw new ModelServiceException("savingsRate must be between -100 and 100.");
        }

        if (request.getPaymentRegularity() == null || Double.isNaN(request.getPaymentRegularity())) {
            throw new ModelServiceException("paymentRegularity is required.");
        }
        if (request.getPaymentRegularity() < 0 || request.getPaymentRegularity() > 1) {
            throw new ModelServiceException("paymentRegularity must be between 0 and 1.");
        }
    }
}
