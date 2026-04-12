package com.gla.credit_scoring_service.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PredictionResponse {

    private Double rawProbability;
    private Integer normalizedCreditScore;
    private String riskTier;
    private String decision;
    private String modelVersion;
    private String modelUsed;
    private Double confidence;
    private Map<String, Double> featureImportance;
    private List<String> reasonCodes;
}