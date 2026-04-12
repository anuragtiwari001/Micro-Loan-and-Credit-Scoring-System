package com.gla.mlmodel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequest {
    private String csvFilePath;
    // Potentially future feature to supply parameters
}
