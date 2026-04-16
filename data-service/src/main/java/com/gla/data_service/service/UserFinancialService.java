package com.gla.data_service.service;

import com.gla.data_service.entity.UserFinancialData;
import com.gla.data_service.repository.UserFinancialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Business logic for financial data CRUD operations.
 *
 * SAVE vs UPDATE:
 *   save()   → creates a new record; throws if one already exists.
 *   update() → patches all fields on the existing record.
 *
 * These are intentionally separate to prevent silent overwrites.
 * The frontend must call /submit once, then /update for any changes.
 */
@Service
public class UserFinancialService {

    @Autowired
    private UserFinancialRepository repo;

    // ─────────────────────────────────────────────────────────────────────────
    // Save (create)
    // ─────────────────────────────────────────────────────────────────────────

    public UserFinancialData save(UserFinancialData data) {
        if (repo.findByEmail(data.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Financial data already exists for: " + data.getEmail() +
                            ". Use the update API instead.");
        }
        return repo.save(data);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (full replace)
    // ─────────────────────────────────────────────────────────────────────────

    public UserFinancialData update(UserFinancialData data) {
        UserFinancialData existing = repo.findByEmail(data.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "No financial data found for: " + data.getEmail() +
                                ". Submit data first."));

        // Update all mutable fields
        existing.setIncome(data.getIncome());
        existing.setExpenses(data.getExpenses());
        existing.setEmploymentType(data.getEmploymentType());
        existing.setExistingLoans(data.getExistingLoans());

        existing.setContactNumber(data.getContactNumber());
        existing.setPanNumber(data.getPanNumber());
        existing.setAadharNumber(data.getAadharNumber());

        existing.setMissedPaymentsCount(data.getMissedPaymentsCount());
        existing.setTotalTransactions(data.getTotalTransactions());
        existing.setEmploymentLengthMonths(data.getEmploymentLengthMonths());
        existing.setAverageBalance(data.getAverageBalance());

        existing.setMonthlyBalanceHistory(data.getMonthlyBalanceHistory());
        existing.setSalaryHistory(data.getSalaryHistory());

        return repo.save(existing);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Read (called by credit-scoring-service via GET /data/get?email=...)
    // ─────────────────────────────────────────────────────────────────────────

    public UserFinancialData getByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Financial data not found for: " + email));
    }
}

//package com.gla.data_service.service;
//
//import com.gla.data_service.entity.UserFinancialData;
//import com.gla.data_service.repository.UserFinancialRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserFinancialService {
//
//    @Autowired
//    private UserFinancialRepository repo;
//
//    public UserFinancialData save(UserFinancialData data) {
//
//        if (repo.findByEmail(data.getEmail()).isPresent()) {
//            throw new RuntimeException("User already exists. Use update API.");
//        }
//
//        return repo.save(data);
//    }
//
//    // ✅ UPDATE (FULL UPDATE)
//    public UserFinancialData update(UserFinancialData data) {
//
//        UserFinancialData existing = repo.findByEmail(data.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // 🔥 UPDATE ALL FIELDS
//        existing.setIncome(data.getIncome());
//        existing.setExpenses(data.getExpenses());
//        existing.setEmploymentType(data.getEmploymentType());
//        existing.setExistingLoans(data.getExistingLoans());
//
//        existing.setContactNumber(data.getContactNumber());
//        existing.setPanNumber(data.getPanNumber());
//        existing.setAadharNumber(data.getAadharNumber());
//
//        existing.setMissedPaymentsCount(data.getMissedPaymentsCount());
//        existing.setTotalTransactions(data.getTotalTransactions());
//        existing.setEmploymentLengthMonths(data.getEmploymentLengthMonths());
//
//        existing.setMonthlyBalanceHistory(data.getMonthlyBalanceHistory());
//        existing.setSalaryHistory(data.getSalaryHistory());
//
//        return repo.save(existing);
//    }
//
//    // ✅ GET (USED BY CREDIT SERVICE)
//    public UserFinancialData getByEmail(String email) {
//        return repo.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//    }
//}