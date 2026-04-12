package com.gla.credit_scoring_service.entity;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserFinancialData {

    private Double income;
    private Double expenses;
    private String employmentType;
    private Double existingLoans;
}
