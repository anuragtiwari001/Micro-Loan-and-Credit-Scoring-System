package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.PredictionRequest;
import com.gla.mlmodel.dto.PredictionResponse;
import com.gla.mlmodel.dto.ModelMetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import smile.classification.Classifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    private final FeatureEngineeringService featureValidation;
    private final RuleBasedFallbackService fallbackService;
    private final RiskClassificationService riskService;
    private final EnsembleScoringService ensembleService;
    private final ModelPersistenceService persistenceService;
    private final ModelMetadataService metadataService;

    // Cached model objects
    private Classifier<double[]> lrModel;
    private Classifier<double[]> rfModel;
    private ModelMetadataResponse metadata;

    public PredictionService(FeatureEngineeringService featureValidation,
                             RuleBasedFallbackService fallbackService,
                             RiskClassificationService riskService,
                             EnsembleScoringService ensembleService,
                             ModelPersistenceService persistenceService,
                             ModelMetadataService metadataService) {
        this.featureValidation = featureValidation;
        this.fallbackService = fallbackService;
        this.riskService = riskService;
        this.ensembleService = ensembleService;
        this.persistenceService = persistenceService;
        this.metadataService = metadataService;
    }

    public synchronized void reloadModels() {
        try {
            logger.info("Attempting to load models from disk...");
            Object lr = persistenceService.loadLogisticRegression();
            Object rf = persistenceService.loadRandomForest();
            
            if (lr instanceof Classifier && rf instanceof Classifier) {
                this.lrModel = (Classifier<double[]>) lr;
                this.rfModel = (Classifier<double[]>) rf;
                this.metadata = metadataService.loadMetadata();
                logger.info("Successfully loaded ML models. Version: {}", metadata != null ? metadata.getModelVersion() : "UNKNOWN");
            } else {
                logger.warn("Loaded objects are not recognized as Smile Classifiers.");
            }
        } catch (Exception e) {
            logger.warn("Models could not be loaded. Service will operate in FALLBACK mode. Reason: {}", e.getMessage());
            this.lrModel = null;
            this.rfModel = null;
            this.metadata = null;
        }
    }

    public boolean areModelsLoaded() {
        return lrModel != null && rfModel != null;
    }

    public ModelMetadataResponse getCurrentMetadata() {
        return metadata;
    }

    public PredictionResponse predict(PredictionRequest request) {
        featureValidation.processAndValidateFeatures(request);

        double[] vec = new double[]{
                request.getDebtToIncomeRatio(),
                request.getIncomeConsistencyScore(),
                request.getSpendingVolatilityIndex(),
                request.getSavingsRate(),
                request.getPaymentRegularity()
        };

        double rawProb = 0.0;
        String modelUsed;
        Map<String, Double> importance;

        if (areModelsLoaded()) {
            try {
                double[] lrPost = new double[2];
                lrModel.predict(vec, lrPost);

                double[] rfPost = new double[2];
                rfModel.predict(vec, rfPost);

                rawProb = ensembleService.calculateEnsembleProbability(lrPost[1], rfPost[1]);
                modelUsed = "ENSEMBLE_RF_LR";
                importance = fallbackService.getDefaultFeatureImportance();
            } catch (Exception e) {
                logger.error("Error during ML inference. Falling back to rule-based.", e);
                rawProb = fallbackService.calculateFallbackProbability(request);
                modelUsed = "RULE_BASED_FALLBACK";
                importance = fallbackService.getDefaultFeatureImportance();
            }
        } else {
            logger.debug("Models missing. Operating in FALLBACK mode.");
            rawProb = fallbackService.calculateFallbackProbability(request);
            modelUsed = "RULE_BASED_FALLBACK";
            importance = fallbackService.getDefaultFeatureImportance();
        }

        int score = riskService.normalizeToCreditScore(rawProb);
        String tier = riskService.determineRiskTier(score);
        String decision = riskService.determineDecision(tier);
        List<String> reasonCodes = riskService.generateReasonCodes(request);

        return PredictionResponse.builder()
                .rawProbability(rawProb)
                .normalizedCreditScore(score)
                .riskTier(tier)
                .decision(decision)
                .modelVersion(metadata != null ? metadata.getModelVersion() : "FALLBACK_v1")
                .modelUsed(modelUsed)
                .confidence(Math.max(rawProb, 1.0 - rawProb))
                .featureImportance(importance)
                .reasonCodes(reasonCodes)
                .build();
    }
}
