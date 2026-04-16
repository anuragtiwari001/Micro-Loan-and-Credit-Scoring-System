package com.gla.credit_scoring_service.service;

import com.gla.credit_scoring_service.dto.*;
import com.gla.credit_scoring_service.entity.CreditScoreHistory;
import com.gla.credit_scoring_service.entity.UserFinancialData;
import com.gla.credit_scoring_service.repository.CreditScoreHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Core service for the credit-scoring-service (:8083).
 *
 * FLOW for a user's own score (getCreditScore):
 *   1. Call data-service GET /api/v1/data/get?email={email}   → UserFinancialData
 *   2. Build a PredictionRequest from the financial data.
 *   3. POST to ml-model-service /api/v1/model/predict         → PredictionResponse
 *   4. Persist/update CreditScoreHistory (one row per user).
 *   5. Return PredictionResponse to controller.
 *
 * FLOW for admin score by userId (getCreditScoreByUserId):
 *   1. Call user-service GET /api/v1/users/email/{userId}     → email
 *   2. Then same as above.
 *
 * FALLBACK:
 *   If the ml-model-service is unreachable or returns an error, a rule-based
 *   fallback score of 650 is used so loan applications are never fully blocked.
 *
 * TIMEOUT:
 *   RestTemplate is configured with connect=3s / read=5s in RestConfig.
 *
 * CONFIG (application.properties):
 *   data.service.url=http://localhost:8082
 *   ml.service.url=http://localhost:8084
 *   user.service.url=http://localhost:8081
 */
@Service
public class CreditScoreService {

    @Value("${data.service.url:http://localhost:8082}")
    private String dataServiceUrl;

    @Value("${ml.service.url:http://localhost:8084}")
    private String mlServiceUrl;

    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;

    /** Fallback score when ML model is unreachable — "review" band (600-749) */
    private static final int FALLBACK_SCORE = 650;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CreditScoreHistoryRepository historyRepo;

    @Autowired
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────
    // Get score for the authenticated user (self)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fetches the credit score for the given email.
     * Caches the result in credit_score_history for audit/dashboard use.
     *
     * @param email email extracted from JWT
     * @param token full "Bearer ..." header to forward to downstream services
     */
    public PredictionResponse getCreditScore(String email, String token) {
        // 1 ── Fetch financial data
        UserFinancialData financialData = fetchFinancialData(email, token);

        // 2 ── Build ML prediction request
        PredictionRequest predictionRequest = buildPredictionRequest(financialData);

        // 3 ── Call ML model (with fallback)
        PredictionResponse response = callMlModel(predictionRequest, token);

        // 4 ── Persist result
        persistHistory(email, response);

        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get score for any user by userId (admin only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Admin endpoint: resolves userId → email, then runs full credit analysis.
     *
     * @param userId  the user's primary key from gla_users database
     * @param token   admin's "Bearer ..." header
     */
    public PredictionResponse getCreditScoreByUserId(Long userId, String token) {
        String email = fetchEmailByUserId(userId, token);
        return getCreditScore(email, token);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1: Fetch financial data from data-service
    // ─────────────────────────────────────────────────────────────────────────

    private UserFinancialData fetchFinancialData(String email, String token) {
        String url = dataServiceUrl + "/api/v1/data/get?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            // data-service now returns ApiResponse<UserFinancialData>
            ResponseEntity<Map> raw = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (raw.getBody() == null || raw.getBody().get("data") == null) {
                throw new RuntimeException(
                        "No financial data found for email: " + email);
            }

            // Deserialize the "data" field from the ApiResponse wrapper
            return objectMapper.convertValue(
                    raw.getBody().get("data"), UserFinancialData.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to fetch financial data: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2: Build ML prediction request from financial data
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps UserFinancialData fields to the PredictionRequest DTO expected
     * by ml-model-service. Also computes derived features (ratios, scores).
     */
    private PredictionRequest buildPredictionRequest(UserFinancialData d) {
        PredictionRequest req = new PredictionRequest();

        req.setMonthlyIncome(d.getIncome());
        req.setMonthlyExpenses(d.getExpenses());
        req.setExistingEMIs(d.getExistingLoans());
        req.setAverageBalance(d.getAverageBalance());
        req.setMissedPaymentsCount(d.getMissedPaymentsCount());
        req.setTotalTransactions(d.getTotalTransactions());
        req.setEmploymentLengthMonths(d.getEmploymentLengthMonths());

        // ── Derived features (FeatureEngineeringService logic replicated here) ──

        // Debt-to-income ratio: (expenses + existing EMIs) / income
        if (d.getIncome() != null && d.getIncome() > 0) {
            double expenses   = d.getExpenses() != null ? d.getExpenses() : 0;
            double existingEMI = d.getExistingLoans() != null ? d.getExistingLoans() : 0;
            req.setDebtToIncomeRatio((expenses + existingEMI) / d.getIncome());

            // Savings rate: (income - expenses) / income
            req.setSavingsRate((d.getIncome() - expenses) / d.getIncome());
        }

        // Payment regularity: 1 - (missedPayments / max(totalTransactions,1))
        if (d.getMissedPaymentsCount() != null && d.getTotalTransactions() != null) {
            int total  = Math.max(d.getTotalTransactions(), 1);
            double reg = 1.0 - ((double) d.getMissedPaymentsCount() / total);
            req.setPaymentRegularity(Math.max(0.0, reg));
        }

        // Income consistency: computed from salary history CSV variance
        if (d.getSalaryHistory() != null && !d.getSalaryHistory().isBlank()) {
            req.setIncomeConsistencyScore(
                    computeConsistencyScore(d.getSalaryHistory()));
        }

        // Spending volatility: computed from balance history CSV variance
        if (d.getMonthlyBalanceHistory() != null &&
                !d.getMonthlyBalanceHistory().isBlank()) {
            req.setSpendingVolatilityIndex(
                    computeVolatilityIndex(d.getMonthlyBalanceHistory()));
        }

        return req;
    }

    /** Returns 0.0 (highly volatile) to 1.0 (perfectly consistent) */
    private double computeConsistencyScore(String csv) {
        try {
            double[] vals = parseCsv(csv);
            if (vals.length < 2) return 1.0;
            double mean = average(vals);
            if (mean == 0) return 1.0;
            double stdDev = standardDeviation(vals, mean);
            double cv = stdDev / mean;           // coefficient of variation
            return Math.max(0.0, 1.0 - cv);
        } catch (Exception e) {
            return 0.5;  // neutral fallback
        }
    }

    /** Returns 0.0 (stable) to 1.0 (highly volatile) */
    private double computeVolatilityIndex(String csv) {
        try {
            double[] vals = parseCsv(csv);
            if (vals.length < 2) return 0.0;
            double mean = average(vals);
            if (mean == 0) return 0.0;
            double stdDev = standardDeviation(vals, mean);
            return Math.min(1.0, stdDev / mean);
        } catch (Exception e) {
            return 0.5;
        }
    }

    private double[] parseCsv(String csv) {
        String[] parts = csv.split(",");
        double[] vals = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vals[i] = Double.parseDouble(parts[i].trim());
        }
        return vals;
    }

    private double average(double[] vals) {
        double sum = 0;
        for (double v : vals) sum += v;
        return sum / vals.length;
    }

    private double standardDeviation(double[] vals, double mean) {
        double sumSq = 0;
        for (double v : vals) sumSq += (v - mean) * (v - mean);
        return Math.sqrt(sumSq / vals.length);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 3: Call ML model with fallback
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POSTs the prediction request to ml-model-service.
     * If the service is down or throws, returns a deterministic fallback response
     * so loan applications are never blocked by an ML service outage.
     */
    private PredictionResponse callMlModel(PredictionRequest req, String token) {
        try {
            // ✅ FIXED: Use the correct endpoint from ModelController
            String url = mlServiceUrl + "/api/v1/model/predict";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            HttpEntity<PredictionRequest> entity = new HttpEntity<>(req, headers);

            ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, PredictionResponse.class);

            if (response.getBody() == null) {
                return buildFallbackResponse();
            }
            return response.getBody();

        } catch (Exception e) {
            // ML model unreachable — use rule-based fallback
            System.err.println("[CreditScoreService] ML model call failed: "
                    + e.getMessage() + " — using fallback score " + FALLBACK_SCORE);
            return buildFallbackResponse();
        }
    }

    /**
     * Deterministic fallback when the ML model is unavailable.
     * Score of 650 = "FAIR" band → triggers manual REVIEW decision.
     */
    private PredictionResponse buildFallbackResponse() {
        PredictionResponse fallback = new PredictionResponse();
        fallback.setNormalizedCreditScore(FALLBACK_SCORE);
        fallback.setRiskTier("FAIR");
        fallback.setDecision("REVIEW");
        fallback.setModelVersion("fallback-rule-based");
        fallback.setModelUsed("RuleBasedFallback");
        fallback.setConfidence(0.5);
        fallback.setReasonCodes(List.of("ML_SERVICE_UNAVAILABLE"));
        return fallback;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 4: Persist score history
    // ─────────────────────────────────────────────────────────────────────────

    private void persistHistory(String email, PredictionResponse response) {
        try {
            CreditScoreHistory history = historyRepo.findByEmail(email)
                    .orElse(CreditScoreHistory.builder().email(email).build());

            history.setNormalizedCreditScore(response.getNormalizedCreditScore());
            history.setRawProbability(response.getRawProbability());
            history.setRiskTier(response.getRiskTier());
            history.setDecision(response.getDecision());
            history.setFullResponse(
                    objectMapper.writeValueAsString(response));
            history.setUpdatedAt(LocalDateTime.now());

            historyRepo.save(history);
        } catch (Exception e) {
            // History persistence failure must NOT block the score response
            System.err.println("[CreditScoreService] Failed to persist history: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper: resolve userId → email via user-service
    // ─────────────────────────────────────────────────────────────────────────

    private String fetchEmailByUserId(Long userId, String token) {
        // user-service GET /api/v1/admin/user/{id} returns email as string in "data" field
        String url = userServiceUrl + "/api/v1/admin/user/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() == null || response.getBody().get("data") == null) {
                throw new RuntimeException("User not found for id: " + userId);
            }
            return (String) response.getBody().get("data");
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to resolve userId " + userId + ": " + e.getMessage());
        }
    }
}