package com.gla.data_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_financial_data")
public class UserFinancialData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private Double income;
    private Double expenses;
    private String employmentType;
    private Double existingLoans;

    // 🔹 KYC
    private String contactNumber;
    private String panNumber;
    private String aadharNumber;

    // 🔥 Behavioral
    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;

    // 🔥 NEW (IMPORTANT)
    private Double averageBalance;

    // CSV history
    @Column(columnDefinition = "TEXT")
    private String monthlyBalanceHistory;

    @Column(columnDefinition = "TEXT")
    private String salaryHistory;

    // timestamps
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