package com.gla.credit_scoring_service.repository;

import com.gla.credit_scoring_service.entity.CreditScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for credit_score_history table.
 *
 * One row per user (email is unique). Used by CreditScoreService to
 * cache the latest score after each ML call.
 *
 * findByEmail is the primary access pattern — used to upsert the score:
 *   historyRepo.findByEmail(email)
 *     .orElse(CreditScoreHistory.builder().email(email).build())
 */
public interface CreditScoreHistoryRepository
        extends JpaRepository<CreditScoreHistory, Long> {

    Optional<CreditScoreHistory> findByEmail(String email);
}