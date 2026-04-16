package com.gla.credit_scoring_service.entity;

import lombok.Data;

/**
 * Local mirror of data-service's UserFinancialData.
 *
 * This is NOT a JPA entity — it is a plain DTO used to deserialize
 * the payload returned by:
 *   data-service GET /api/v1/data/get?email={email}
 *
 * WHY NOT import data-service entity directly:
 *   Microservices must NOT share entity classes across module boundaries.
 *   Each service deserializes only the fields it needs.
 *   This decouples credit-scoring-service from data-service's internal schema.
 *
 * FIELDS USED BY CreditScoreService.buildPredictionRequest():
 *   income, expenses, existingLoans → debt-to-income ratio
 *   missedPaymentsCount, totalTransactions → payment regularity
 *   employmentLengthMonths → employment stability
 *   averageBalance → balance health
 *   monthlyBalanceHistory → spending volatility index (CSV)
 *   salaryHistory → income consistency score (CSV)
 */
@Data
public class UserFinancialData {

    private Double  income;
    private Double  expenses;
    private String  employmentType;
    private Double  existingLoans;

    private Integer missedPaymentsCount;
    private Integer totalTransactions;
    private Integer employmentLengthMonths;
    private Double  averageBalance;

    private String monthlyBalanceHistory;  // CSV e.g. "80000,82000,79000"
    private String salaryHistory;          // CSV e.g. "60000,60000,62000"
}