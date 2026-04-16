package com.gla.credit_scoring_service.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.credit_scoring_service.dto.AdminCreditRequest;
import com.gla.credit_scoring_service.dto.PredictionResponse;
import com.gla.credit_scoring_service.dto.UserScoreResponse;
import com.gla.credit_scoring_service.service.CreditScoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Credit scoring endpoints for credit-scoring-service (:8083).
 *
 * GET  /api/v1/credit/score         → USER gets their normalized score only
 *                                    → ADMIN gets the full ML response
 * POST /api/v1/credit/admin/score   → ADMIN gets full ML response for any userId
 *
 * HOW IT WORKS:
 *   1. Extract email from JWT attribute (set by JwtFilter).
 *   2. Call CreditScoreService which:
 *        a. Fetches financial data from data-service via REST.
 *        b. Builds a PredictionRequest DTO.
 *        c. Calls ml-model-service (:8084) /predict endpoint.
 *        d. Caches the result in credit_score_history (one row per user).
 *   3. Return appropriate response based on role:
 *        USER  → UserScoreResponse  { normalizedCreditScore: 744 }
 *        ADMIN → PredictionResponse { rawProbability, riskTier, featureImportance, ... }
 */
@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {

    @Autowired
    private CreditScoreService service;

    // ─────────────────────────────────────────────────────────────────────────
    // Self-score (USER or ADMIN calling for their own account)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the credit score for the authenticated user.
     *
     * USER  response: { "data": { "normalizedCreditScore": 744 } }
     * ADMIN response: { "data": { all ML fields } }
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<?>> getScore(HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        String role  = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");

        PredictionResponse fullResponse = service.getCreditScore(email, token);

        if ("USER".equalsIgnoreCase(role)) {
            // Users see only the normalized score
            UserScoreResponse limited =
                    new UserScoreResponse(fullResponse.getNormalizedCreditScore());
            return ResponseEntity.ok(
                    ApiResponse.success("Credit score retrieved", limited));
        }

        // Admins see the full ML response
        return ResponseEntity.ok(
                ApiResponse.success("Credit score retrieved", fullResponse));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin score by userId
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the full ML prediction for any user by their userId.
     * ADMIN role required.
     *
     * Request:  POST { "userId": 1 }
     * Response: { "data": { full PredictionResponse } }
     *
     * This is called by loan-service.getAdminDashboard() when building
     * the admin dashboard with a live fresh credit analysis.
     */
    @PostMapping("/admin/score")
    public ResponseEntity<ApiResponse<PredictionResponse>> getScoreByAdmin(
            @Valid @RequestBody AdminCreditRequest requestBody,
            HttpServletRequest request) {

        String role  = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN role required");
        }

        PredictionResponse response =
                service.getCreditScoreByUserId(requestBody.getUserId(), token);

        return ResponseEntity.ok(
                ApiResponse.success("Full credit analysis retrieved", response));
    }
}

//package com.gla.credit_scoring_service.controller;

//import com.gla.credit_scoring_service.dto.AdminCreditRequest;
//import com.gla.credit_scoring_service.dto.PredictionResponse;
//import com.gla.credit_scoring_service.dto.UserScoreResponse;
//import com.gla.credit_scoring_service.service.CreditScoreService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//@RestController
//@RequestMapping("/api/v1/credit")
//public class CreditController {
//    @Autowired
//    private CreditScoreService service;
//    // ✅ USER / ADMIN self score
//    @GetMapping("/score")
//    public ResponseEntity<?> getScore(HttpServletRequest request) {
//        String email = (String) request.getAttribute("email");
//        String role = (String) request.getAttribute("role");
//        PredictionResponse response =
//                service.getCreditScore(email, request.getHeader("Authorization"));
//        if ("USER".equalsIgnoreCase(role)) {
//            return ResponseEntity.ok(
//                    new UserScoreResponse(response.getNormalizedCreditScore())
//            );
//        }
//        return ResponseEntity.ok(response);
//    }
//    // 🔥 ADMIN FULL CHECK (FIXED)
//    @PostMapping("/admin/score")
//    public ResponseEntity<?> getScoreByAdmin(
//            @RequestBody AdminCreditRequest requestBody,
//            HttpServletRequest request
//    ) {
//        String role = (String) request.getAttribute("role");
//        String token = request.getHeader("Authorization");
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            throw new RuntimeException("Access Denied: ADMIN only");
//        }
//        return ResponseEntity.ok(
//                service.getCreditScoreByUserId(
//                        requestBody.getUserId(),
//                        token
//                )
//        );
//    }
//}
//

//package com.gla.credit_scoring_service.controller;
//import com.gla.credit_scoring_service.dto.PredictionResponse;
//import com.gla.credit_scoring_service.dto.UserScoreResponse;
//import com.gla.credit_scoring_service.service.CreditScoreService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/credit")
//public class CreditController {
//
//    @Autowired
//    private CreditScoreService service;
//
//    @GetMapping("/score")
//    public ResponseEntity<?> getScore(HttpServletRequest request) {
//
//        String email = (String) request.getAttribute("email");
//        String role = (String) request.getAttribute("role");
//
//        PredictionResponse response =
//                service.getCreditScore(email, request.getHeader("Authorization"));
//
//        // 👤 USER → limited response
//        if ("USER".equalsIgnoreCase(role)) {
//            return ResponseEntity.ok(
//                    new UserScoreResponse(response.getNormalizedCreditScore())
//            );
//        }
//
//        // 🧑‍💼 ADMIN → full response
//        return ResponseEntity.ok(response);
//    }
//}

/*
package com.gla.credit_scoring_service.controller;
import com.gla.credit_scoring_service.dto.AdminCreditRequest;
import com.gla.credit_scoring_service.dto.PredictionResponse;
import com.gla.credit_scoring_service.dto.UserScoreResponse;
import com.gla.credit_scoring_service.service.CreditScoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditController {

    @Autowired
    private CreditScoreService service;

    // 🔹 USER / ADMIN → SELF SCORE
    @GetMapping("/score")
    public ResponseEntity<?> getScore(HttpServletRequest request) {

        // ✅ Extract email & role
        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");

        // ✅ FIX: pass role also
        PredictionResponse response =
                service.getCreditScore(
                        email,
                        request.getHeader("Authorization"),
                        role   // 🔥 ADD THIS
                );

        // 👤 USER → only score
        if ("USER".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(
                    new UserScoreResponse(response.getNormalizedCreditScore())
            );
        }

        // 🧑‍💼 ADMIN → full response
        return ResponseEntity.ok(response);
    }
    /*
    @GetMapping("/score")
    public ResponseEntity<?> getScore(HttpServletRequest request) {

        // ✅ Extract from JWT
        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");

        // 🔥 Service handles everything (including role-based response)
        return ResponseEntity.ok(
                service.getCreditScore(email, token, role)
        );
    }



    // 🔹 ADMIN → GET ANY USER SCORE BY ID
    @PostMapping("/admin/score")
    public ResponseEntity<?> getScoreByAdmin(
            @RequestBody AdminCreditRequest requestBody,
            HttpServletRequest request
    ) {

        String role = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");

        // 🔐 SECURITY CHECK
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN only");
        }

        return ResponseEntity.ok(
                service.getCreditScoreByUserId(
                        requestBody.getUserId(),
                        token,
                        role
                )
        );
    }
}
*/
