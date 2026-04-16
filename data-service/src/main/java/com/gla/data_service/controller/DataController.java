package com.gla.data_service.controller;

import com.gla.common_service.dto.ApiResponse;
import com.gla.data_service.entity.UserFinancialData;
import com.gla.data_service.service.UserFinancialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Financial data endpoints for data-service (:8082).
 *
 * POST /api/v1/data/submit   → save financial data (USER, call once)
 * PUT  /api/v1/data/update   → update financial data (USER)
 * GET  /api/v1/data/get      → fetch by email (USER sees own; ADMIN sees any)
 *
 * SECURITY:
 *   All endpoints require a valid JWT (enforced by SecurityConfig).
 *   The user's email is taken from the JWT attribute, NOT from the request body,
 *   so a user can never submit or read data for a different account.
 */
@RestController
@RequestMapping("/api/v1/data")
public class DataController {

    @Autowired
    private UserFinancialService service;

    // ─────────────────────────────────────────────────────────────────────────
    // Submit (create)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves financial data for the authenticated user.
     * Call this only once; use /update for subsequent changes.
     *
     * Email is extracted from the JWT — the request body `email` field is ignored.
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<UserFinancialData>> submit(
            @Valid @RequestBody UserFinancialData data,
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        data.setEmail(email);  // enforce identity from JWT

        UserFinancialData saved = service.save(data);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Financial data submitted", saved));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates all financial fields for the authenticated user.
     * Requires an existing record (call /submit first).
     */
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<UserFinancialData>> update(
            @Valid @RequestBody UserFinancialData data,
            HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        data.setEmail(email);

        UserFinancialData updated = service.update(data);
        return ResponseEntity.ok(ApiResponse.success("Financial data updated", updated));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Get — used by credit-scoring-service (internal call with ADMIN token)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns financial data for the given email.
     *
     * Role-based access:
     *   USER  → can only retrieve their own data (email must match JWT)
     *   ADMIN → can retrieve any user's data (for credit score calls)
     *
     * @param email the email whose data is requested (query param)
     */
    @GetMapping("/get")
    public ResponseEntity<ApiResponse<UserFinancialData>> get(
            @RequestParam String email,
            HttpServletRequest request) {

        String tokenEmail = (String) request.getAttribute("email");
        String role       = (String) request.getAttribute("role");

        if ("USER".equalsIgnoreCase(role) && !email.equals(tokenEmail)) {
            throw new RuntimeException("Unauthorized: cannot access another user's data");
        }

        UserFinancialData data = service.getByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Data retrieved", data));
    }
}


//package com.gla.data_service.controller;
//
//import com.gla.data_service.entity.UserFinancialData;
//import com.gla.data_service.service.UserFinancialService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/data")
//public class DataController {
//
//    @Autowired
//    private UserFinancialService service;
//
//    // ✅ SAVE (USER only)
//    @PostMapping("/submit")
//    public UserFinancialData save(@RequestBody UserFinancialData data,
//                                  HttpServletRequest request) {
//
//        String emailFromToken = (String) request.getAttribute("email");
//
//        // 🔥 enforce user identity
//        data.setEmail(emailFromToken);
//
//        return service.save(data);
//    }
//
//    // ✅ UPDATE (USER only)
//    @PutMapping("/update")
//    public UserFinancialData update(@RequestBody UserFinancialData data,
//                                    HttpServletRequest request) {
//
//        String emailFromToken = (String) request.getAttribute("email");
//
//        data.setEmail(emailFromToken);
//
//        return service.update(data);
//    }
//
//    // ✅ GET (USED BY CREDIT SERVICE + ADMIN)
//    @GetMapping("/get")
//    public UserFinancialData get(@RequestParam String email,
//                                 HttpServletRequest request) {
//
//        String tokenEmail = (String) request.getAttribute("email");
//        String role = (String) request.getAttribute("role");
//
//        // 🔐 ROLE-BASED ACCESS CONTROL
//        if ("USER".equalsIgnoreCase(role)) {
//            // USER → can access only own data
//            if (!email.equals(tokenEmail)) {
//                throw new RuntimeException("Unauthorized access");
//            }
//        }
//
//        // ADMIN → allowed for any email (no restriction)
//
//        return service.getByEmail(email);
//    }
//}