// ════════════════════════════════════════════════════════════════════════════
// FILE: com/gla/credit_scoring_service/dto/UserScoreResponse.java
// ════════════════════════════════════════════════════════════════════════════
package com.gla.credit_scoring_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Limited response shown to USER role.
 * Hides raw probability, feature importance, reason codes, and model metadata.
 *
 * Frontend usage:
 *   const score = response.data.data.normalizedCreditScore;  // e.g. 744
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScoreResponse {
    private Integer normalizedCreditScore;
}