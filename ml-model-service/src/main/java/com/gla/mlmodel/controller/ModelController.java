package com.gla.mlmodel.controller;

import com.gla.mlmodel.dto.*;
import com.gla.mlmodel.service.PredictionService;
import com.gla.mlmodel.service.TrainingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/model")
public class ModelController {

    private final PredictionService predictionService;
    private final TrainingService trainingService;

    public ModelController(PredictionService predictionService, TrainingService trainingService) {
        this.predictionService = predictionService;
        this.trainingService = trainingService;
    }

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> predict(@RequestBody PredictionRequest request) {
        PredictionResponse response = predictionService.predict(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/train")
    public ResponseEntity<TrainingResponse> train(@RequestBody TrainingRequest request) {
        TrainingResponse response = trainingService.trainModels(request);
        if (response.isSuccess()) {
            predictionService.reloadModels(); // Reload automatically after a successful train
        }
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> reload() {
        predictionService.reloadModels();
        Map<String, String> body = new HashMap<>();
        if (predictionService.areModelsLoaded()) {
            body.put("status", "SUCCESS");
            body.put("message", "Models loaded successfully.");
            return ResponseEntity.ok(body);
        } else {
            body.put("status", "FAILED");
            body.put("message", "Models could not be loaded. Operating in fallback mode.");
            return ResponseEntity.internalServerError().body(body);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .modelsLoaded(predictionService.areModelsLoaded())
                .currentVersion(predictionService.getCurrentMetadata() != null ? predictionService.getCurrentMetadata().getModelVersion() : "N/A")
                .message(predictionService.areModelsLoaded() ? "Service is fully operational" : "Service is running in fallback mode")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metadata")
    public ResponseEntity<ModelMetadataResponse> metadata() {
        ModelMetadataResponse meta = predictionService.getCurrentMetadata();
        if (meta != null) {
            return ResponseEntity.ok(meta);
        }
        return ResponseEntity.notFound().build();
    }
}
