package com.gla.credit_scoring_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(unique = true) // 🔥 IMPORTANT (one record per user)
    private String email;

    private Integer normalizedCreditScore;
    private Double rawProbability;
    private String riskTier;
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String fullResponse;

    private LocalDateTime updatedAt;
}