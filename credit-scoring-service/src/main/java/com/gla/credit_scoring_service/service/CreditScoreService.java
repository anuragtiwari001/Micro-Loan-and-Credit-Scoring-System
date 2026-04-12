package com.gla.credit_scoring_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gla.credit_scoring_service.dto.PredictionRequest;
import com.gla.credit_scoring_service.dto.PredictionResponse;
import com.gla.credit_scoring_service.dto.UserFinancialDataDTO;
import com.gla.credit_scoring_service.entity.CreditScoreHistory;
import com.gla.credit_scoring_service.repository.CreditScoreHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class CreditScoreService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CreditScoreHistoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔥 FEATURE ENGINEERING
    public PredictionRequest buildRequest(UserFinancialDataDTO data) {

        PredictionRequest req = new PredictionRequest();

        double income = data.getIncome();
        double expenses = data.getExpenses();

        req.setDebtToIncomeRatio(data.getExistingLoans() / income);
        req.setSavingsRate((income - expenses) / income);
        req.setPaymentRegularity(1.0 - (data.getMissedPaymentsCount() * 0.1));
        req.setIncomeConsistencyScore(Math.min(1.0, data.getEmploymentLengthMonths() / 60.0));
        req.setSpendingVolatilityIndex(1.0 / (1 + data.getTotalTransactions()));

        req.setMonthlyIncome(income);
        req.setMonthlyExpenses(expenses);
        req.setExistingEMIs(data.getExistingLoans());
        req.setAverageBalance(data.getAverageBalance());

        return req;
    }

    // ✅ SHARED INTERNAL METHOD — fetches fresh score for a given email
    // Used by both USER self-score and ADMIN flow
    private PredictionResponse fetchFreshScore(String email, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // 🔹 1. DATA SERVICE CALL — admin token is valid, ADMIN role passes DataController check
        ResponseEntity<UserFinancialDataDTO> response =
                restTemplate.exchange(
                        "http://localhost:8082/api/v1/data/get?email=" + email,
                        HttpMethod.GET,
                        entity,
                        UserFinancialDataDTO.class
                );

        UserFinancialDataDTO data = response.getBody();

        if (data == null) {
            throw new RuntimeException("User financial data not found for: " + email);
        }

        // 🔹 2. BUILD ML REQUEST
        PredictionRequest request = buildRequest(data);

        // 🔹 3. ML SERVICE CALL
        ResponseEntity<PredictionResponse> mlResponse =
                restTemplate.postForEntity(
                        "http://localhost:8084/api/v1/model/predict",
                        request,
                        PredictionResponse.class
                );

        PredictionResponse prediction = mlResponse.getBody();

        if (prediction == null) {
            throw new RuntimeException("ML service failed");
        }

        // 🔹 4. SAVE / UPDATE
        saveOrUpdateScore(email, prediction);

        return prediction;
    }

    // ✅ USER SELF-SCORE — email comes from JWT token in controller
    public PredictionResponse getCreditScore(String email, String token) {
        return fetchFreshScore(email, token);
    }

    // ✅ ADMIN FLOW — receives userId, resolves email via user-service, fetches fresh score
    public PredictionResponse getCreditScoreByUserId(Long userId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // 🔹 1. GET EMAIL FROM USER-SERVICE using admin endpoint
        ResponseEntity<String> userResponse =
                restTemplate.exchange(
                        "http://localhost:8081/api/v1/admin/user/" + userId,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        String email = userResponse.getBody();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("User not found for userId: " + userId);
        }

        // 🔹 2. FETCH FRESH SCORE (full ML response) using admin token
        // DataController allows ADMIN role to fetch any user's data — this works correctly
        return fetchFreshScore(email, token);
    }

    // 🔥 UPSERT
    public void saveOrUpdateScore(String email, PredictionResponse response) {

        CreditScoreHistory history = repository.findByEmail(email)
                .orElse(CreditScoreHistory.builder().email(email).build());

        history.setNormalizedCreditScore(response.getNormalizedCreditScore());
        history.setRawProbability(response.getRawProbability());
        history.setRiskTier(response.getRiskTier());
        history.setDecision(response.getDecision());

        try {
            history.setFullResponse(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            throw new RuntimeException("JSON conversion error");
        }

        history.setUpdatedAt(LocalDateTime.now());

        repository.save(history);
    }
}

//package com.gla.credit_scoring_service.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.gla.credit_scoring_service.dto.PredictionRequest;
//import com.gla.credit_scoring_service.dto.PredictionResponse;
//import com.gla.credit_scoring_service.dto.UserFinancialDataDTO;
//import com.gla.credit_scoring_service.entity.CreditScoreHistory;
//import com.gla.credit_scoring_service.repository.CreditScoreHistoryRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//
//@Service
//public class CreditScoreService {
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private CreditScoreHistoryRepository repository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // 🔥 FEATURE ENGINEERING
//    public PredictionRequest buildRequest(UserFinancialDataDTO data) {
//
//        PredictionRequest req = new PredictionRequest();
//
//        double income = data.getIncome();
//        double expenses = data.getExpenses();
//
//        req.setDebtToIncomeRatio(data.getExistingLoans() / income);
//        req.setSavingsRate((income - expenses) / income);
//        req.setPaymentRegularity(1.0 - (data.getMissedPaymentsCount() * 0.1));
//        req.setIncomeConsistencyScore(Math.min(1.0, data.getEmploymentLengthMonths() / 60.0));
//        req.setSpendingVolatilityIndex(1.0 / (1 + data.getTotalTransactions()));
//
//        req.setMonthlyIncome(income);
//        req.setMonthlyExpenses(expenses);
//        req.setExistingEMIs(data.getExistingLoans());
//        req.setAverageBalance(data.getAverageBalance());
//
//        return req;
//    }
//
//    // ✅ CLEAN METHOD (NO ROLE)
//    public PredictionResponse getCreditScore(String email, String token) {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", token);
//
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        // 🔹 1. DATA SERVICE CALL
//        ResponseEntity<UserFinancialDataDTO> response =
//                restTemplate.exchange(
//                        "http://localhost:8082/api/v1/data/get?email=" + email,
//                        HttpMethod.GET,
//                        entity,
//                        UserFinancialDataDTO.class
//                );
//
//        UserFinancialDataDTO data = response.getBody();
//
//        if (data == null) {
//            throw new RuntimeException("User financial data not found");
//        }
//
//        // 🔹 2. BUILD ML REQUEST
//        PredictionRequest request = buildRequest(data);
//
//        // 🔹 3. ML SERVICE CALL
//        ResponseEntity<PredictionResponse> mlResponse =
//                restTemplate.postForEntity(
//                        "http://localhost:8084/api/v1/model/predict",
//                        request,
//                        PredictionResponse.class
//                );
//
//        PredictionResponse prediction = mlResponse.getBody();
//
//        if (prediction == null) {
//            throw new RuntimeException("ML service failed");
//        }
//
//        // 🔥 4. SAVE
//        saveOrUpdateScore(email, prediction);
//
//        return prediction;
//    }
//
//    // 🔥 ADMIN FLOW (NO ROLE NOW)
//    public PredictionResponse getCreditScoreByUserId(Long userId, String token) {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", token);
//
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> userResponse =
//                restTemplate.exchange(
//                        "http://localhost:8081/api/v1/admin/user/" + userId,
//                        HttpMethod.GET,
//                        entity,
//                        String.class
//                );
//
//        String email = userResponse.getBody();
//
//        if (email == null) {
//            throw new RuntimeException("User not found");
//        }
//
//        return getCreditScore(email, token);
//    }
//
//    // 🔥 UPSERT
//    public void saveOrUpdateScore(String email, PredictionResponse response) {
//
//        CreditScoreHistory history = repository.findByEmail(email)
//                .orElse(CreditScoreHistory.builder().email(email).build());
//
//        history.setNormalizedCreditScore(response.getNormalizedCreditScore());
//        history.setRawProbability(response.getRawProbability());
//        history.setRiskTier(response.getRiskTier());
//        history.setDecision(response.getDecision());
//
//        try {
//            history.setFullResponse(objectMapper.writeValueAsString(response));
//        } catch (Exception e) {
//            throw new RuntimeException("JSON conversion error");
//        }
//
//        history.setUpdatedAt(LocalDateTime.now());
//
//        repository.save(history);
//    }
//}
