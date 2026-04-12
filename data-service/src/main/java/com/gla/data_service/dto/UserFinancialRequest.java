package com.gla.data_service.dto;

import lombok.Data;

@Data
public class UserFinancialRequest {

    private String email; // optional for update

    private Double income;
    private Double expenses;
    private String employmentType;
    private Double existingLoans;

    // ✅ NEW FIELDS
    private String contactNumber;
    private String panNumber;
    private String aadharNumber;
}