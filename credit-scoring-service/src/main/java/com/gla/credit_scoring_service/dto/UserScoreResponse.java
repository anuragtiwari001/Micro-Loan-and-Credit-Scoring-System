package com.gla.credit_scoring_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserScoreResponse {

    private Integer normalizedCreditScore;
}