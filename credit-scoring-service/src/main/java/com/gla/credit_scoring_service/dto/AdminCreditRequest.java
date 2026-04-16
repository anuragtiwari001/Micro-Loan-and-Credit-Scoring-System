// ════════════════════════════════════════════════════════════════════════════
// FILE: com/gla/credit_scoring_service/dto/AdminCreditRequest.java
// ════════════════════════════════════════════════════════════════════════════
package com.gla.credit_scoring_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request body for POST /api/v1/credit/admin/score.
 *
 * userId is the primary key from the gla_users.user table.
 * Find it with: SELECT id, email FROM user;
 */
@Data
public class AdminCreditRequest {

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be a positive integer")
    private Long userId;
}