package com.gla.data_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for the `user_financial_data` table in gla_data database.
 *
 * FIELDS:
 *   Core financials   : income, expenses, existingLoans, employmentType
 *   KYC identifiers   : panNumber, aadharNumber, contactNumber
 *   Behavioral signals: missedPaymentsCount, totalTransactions,
 *                       employmentLengthMonths, averageBalance
 *   History (CSV)     : monthlyBalanceHistory, salaryHistory
 *                       Format: "80000,82000,79000" (comma-separated doubles)
 *
 * DESIGN NOTES:
 *   - One record per user (email is unique).
 *   - Call /submit to create, /update to modify — prevents accidental duplicates.
 *   - PAN and Aadhaar patterns are validated with regex.
 *   - averageBalance is critical for the ML model (feature engineering).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_financial_data",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserFinancialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Set from JWT — never trust request body for this field */
    @Column(nullable = false, unique = true)
    private String email;

    // ── Core financials ───────────────────────────────────────────────────────

    @NotNull(message = "Income is required")
    @Positive(message = "Income must be positive")
    private Double income;

    @NotNull(message = "Expenses are required")
    @PositiveOrZero(message = "Expenses cannot be negative")
    private Double expenses;

    @NotBlank(message = "Employment type is required")
    private String employmentType;  // SALARIED | SELF_EMPLOYED | UNEMPLOYED

    @PositiveOrZero(message = "Existing loans cannot be negative")
    private Double existingLoans;

    // ── KYC ──────────────────────────────────────────────────────────────────

    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
            message = "PAN number format: ABCDE1234F")
    private String panNumber;

    @Pattern(regexp = "^[0-9]{12}$",
            message = "Aadhaar number must be exactly 12 digits")
    private String aadharNumber;

    // ── Behavioral signals (used by ML model) ─────────────────────────────────

    @Min(value = 0, message = "Missed payments count cannot be negative")
    private Integer missedPaymentsCount;

    @Min(value = 0, message = "Total transactions cannot be negative")
    private Integer totalTransactions;

    @Min(value = 0, message = "Employment length cannot be negative")
    private Integer employmentLengthMonths;

    @PositiveOrZero(message = "Average balance cannot be negative")
    private Double averageBalance;

    // ── History strings (CSV format) ──────────────────────────────────────────

    /**
     * Monthly account balance history as comma-separated values.
     * Example: "80000,82000,79000,85000,83000"
     * Used by ML model for spending volatility and trend analysis.
     */
    @Column(columnDefinition = "TEXT")
    private String monthlyBalanceHistory;

    /**
     * Monthly salary history as comma-separated values.
     * Example: "60000,60000,60000,62000,62000"
     * Used by ML model for income consistency scoring.
     */
    @Column(columnDefinition = "TEXT")
    private String salaryHistory;

    // ── Audit timestamps ──────────────────────────────────────────────────────

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


//package com.gla.data_service.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Table(name = "user_financial_data")
//public class UserFinancialData {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String email;
//
//    private Double income;
//    private Double expenses;
//    private String employmentType;
//    private Double existingLoans;
//
//    // 🔹 KYC
//    private String contactNumber;
//    private String panNumber;
//    private String aadharNumber;
//
//    // 🔥 Behavioral
//    private Integer missedPaymentsCount;
//    private Integer totalTransactions;
//    private Integer employmentLengthMonths;
//
//    // 🔥 NEW (IMPORTANT)
//    private Double averageBalance;
//
//    // CSV history
//    @Column(columnDefinition = "TEXT")
//    private String monthlyBalanceHistory;
//
//    @Column(columnDefinition = "TEXT")
//    private String salaryHistory;
//
//    // timestamps
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
//}