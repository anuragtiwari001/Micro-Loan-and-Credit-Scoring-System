package com.gla.mlmodel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
