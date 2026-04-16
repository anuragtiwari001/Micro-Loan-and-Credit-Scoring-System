package com.gla.loan_service.dto;

import com.gla.loan_service.entity.LoanApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Typed response for GET /api/v1/admin/loans/dashboard/{id}.
 *
 * ORIGINAL ISSUE:
 *   The original AdminDashboardResponse used Object for all four fields,
 *   which made it impossible for callers to know the shape of the response.
 *
 * FIX:
 *   loan         → LoanApplication entity (typed)
 *   documents    → Object (list of DocumentResponse from document-service)
 *   creditDetails→ Object (PredictionResponse from credit-scoring-service)
 *                  These two remain Object because their types live in other
 *                  services — microservices must not cross-import entity types.
 *   recommendation → "APPROVE" | "REVIEW" | "REJECT"
 *
 * FRONTEND NOTE:
 *   recommendation is advisory. The admin makes the final decision via
 *   PUT /approve/{id} or PUT /reject/{id}.
 *
 *   Score thresholds:
 *     >= 750 → APPROVE
 *     >= 600 → REVIEW
 *      < 600 → REJECT
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {

    /** Full loan application including all submitted fields */
    private LoanApplication loan;

    /** List of DocumentResponse objects from document-service (Cloudinary URLs) */
    private Object documents;

    /** Full PredictionResponse from credit-scoring-service (live ML analysis) */
    private Object creditDetails;

    /** Advisory recommendation: APPROVE | REVIEW | REJECT */
    private String recommendation;
}