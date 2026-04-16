package com.gla.credit_scoring_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA entity persisted in gla_credit database — table: credit_score_history.
 *
 * DESIGN: One row per user (email is unique).
 * Every time getCreditScore() is called the existing row is overwritten
 * so the table always holds the most recent score per user.
 *
 * fullResponse stores the complete JSON from the ML model for audit trails.
 *
 * WHY CACHE HERE:
 *   The admin dashboard calls getCreditScoreByUserId() which triggers
 *   a live ML prediction. Storing the result here means the loan-service
 *   could later query cached scores without re-calling the ML model,
 *   which is useful for performance optimization in v2.
 */
@Entity
@Table(name = "credit_score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One record per user — unique constraint enforced at DB level */
    @Column(unique = true, nullable = false)
    private String email;

    private Integer normalizedCreditScore;
    private Double  rawProbability;

    /** EXCELLENT | VERY_GOOD | GOOD | FAIR | POOR */
    private String riskTier;

    /** APPROVE | REVIEW | REJECT */
    private String decision;

    /** Full serialized JSON of PredictionResponse — for audit */
    @Column(columnDefinition = "TEXT")
    private String fullResponse;

    /** Timestamp of the last score computation */
    private LocalDateTime updatedAt;
}