package com.gla.mlmodel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResponse {
    private boolean success;
    private String message;
    private String modelVersion;
    private Map<String, Object> metrics;
}
