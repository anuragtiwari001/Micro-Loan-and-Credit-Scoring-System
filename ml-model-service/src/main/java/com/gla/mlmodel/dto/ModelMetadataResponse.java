package com.gla.mlmodel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMetadataResponse {
    private String modelVersion;
    private String trainedAt;
    private List<String> featureList;
    private Map<String, Object> trainingMetrics;
    private Map<String, String> algorithmDetails;
}
