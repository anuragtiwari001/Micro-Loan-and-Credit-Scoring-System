package com.gla.loan_service.repository;


import com.gla.loan_service.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByEmail(String email);
}