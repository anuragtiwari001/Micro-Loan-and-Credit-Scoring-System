package com.gla.data_service.repository;

import com.gla.data_service.entity.UserFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for the user_financial_data table in gla_data database.
 *
 * Access pattern: always by email (one record per user).
 * findByEmail is used for:
 *   - save()   → check duplicate before insert
 *   - update() → find existing record to patch
 *   - get()    → fetch for credit-scoring-service
 */
public interface UserFinancialRepository
        extends JpaRepository<UserFinancialData, Long> {

    Optional<UserFinancialData> findByEmail(String email);
}