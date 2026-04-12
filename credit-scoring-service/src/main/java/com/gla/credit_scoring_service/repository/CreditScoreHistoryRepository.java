package com.gla.credit_scoring_service.repository;

import com.gla.credit_scoring_service.entity.CreditScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditScoreHistoryRepository
        extends JpaRepository<CreditScoreHistory, Long> {

    Optional<CreditScoreHistory> findByEmail(String email);
}