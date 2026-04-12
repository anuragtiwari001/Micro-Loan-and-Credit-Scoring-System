package com.gla.loan_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 🔥 IMPORTANT FIX
public class CreditResponse {

    private Integer normalizedCreditScore;
}