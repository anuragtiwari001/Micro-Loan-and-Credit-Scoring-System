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
    // ✅ USER / ADMIN self score
    @GetMapping("/score")
    public ResponseEntity<?> getScore(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");
        PredictionResponse response =
                service.getCreditScore(email, request.getHeader("Authorization"));
        if ("USER".equalsIgnoreCase(role)) {
            return ResponseEntity.ok(
                    new UserScoreResponse(response.getNormalizedCreditScore())
            );
        }
        return ResponseEntity.ok(response);
    }
    // 🔥 ADMIN FULL CHECK (FIXED)
    @PostMapping("/admin/score")
    public ResponseEntity<?> getScoreByAdmin(
            @RequestBody AdminCreditRequest requestBody,
            HttpServletRequest request
    ) {
        String role = (String) request.getAttribute("role");
        String token = request.getHeader("Authorization");
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Access Denied: ADMIN only");
        }
        return ResponseEntity.ok(
                service.getCreditScoreByUserId(
                        requestBody.getUserId(),
                        token
                )
        );
    }
}


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
