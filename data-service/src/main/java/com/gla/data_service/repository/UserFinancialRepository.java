package com.gla.data_service.repository;

import com.gla.data_service.entity.UserFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFinancialRepository extends JpaRepository<UserFinancialData, Long> {

    Optional<UserFinancialData> findByEmail(String email);
}