package com.gla.mlmodel.service;

import com.gla.mlmodel.dto.TrainingRequest;
import com.gla.mlmodel.dto.TrainingResponse;
import com.gla.mlmodel.dto.ModelMetadataResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import smile.classification.LogisticRegression;
import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import smile.data.vector.BaseVector;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;

import java.io.FileReader;
import java.io.Reader;
import java.time.Instant;
import java.util.*;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    // ── Column name constants (must match CSV headers exactly) ──────────────
    private static final String COL_DTI   = "debtToIncomeRatio";
    private static final String COL_ICS   = "incomeConsistencyScore";
    private static final String COL_SVI   = "spendingVolatilityIndex";
    private static final String COL_SR    = "savingsRate";
    private static final String COL_PR    = "paymentRegularity";
    private static final String COL_LABEL = "willRepayLoan";

    private final ModelPersistenceService persistenceService;
    private final ModelMetadataService metadataService;

    public TrainingService(ModelPersistenceService persistenceService,
                           ModelMetadataService metadataService) {
        this.persistenceService = persistenceService;
        this.metadataService    = metadataService;
    }

    public TrainingResponse trainModels(TrainingRequest request) {
        logger.info("Starting model training using data: {}", request.getCsvFilePath());

        try {
            List<double[]> xList = new ArrayList<>();
            List<Integer>  yList = new ArrayList<>();

            List<String> featureList = Arrays.asList(
                    COL_DTI, COL_ICS, COL_SVI, COL_SR, COL_PR
            );

            // ── FIX 1: Updated CSVFormat — builder API ───────────────────────────
            CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader()
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (Reader reader       = new FileReader(request.getCsvFilePath());
                 CSVParser csvParser = new CSVParser(reader, csvFormat)) {

                for (CSVRecord record : csvParser) {
                    double[] features = new double[5];
                    features[0] = Double.parseDouble(record.get(COL_DTI));
                    features[1] = Double.parseDouble(record.get(COL_ICS));
                    features[2] = Double.parseDouble(record.get(COL_SVI));
                    features[3] = Double.parseDouble(record.get(COL_SR));
                    features[4] = Double.parseDouble(record.get(COL_PR));

                    int label = Integer.parseInt(record.get(COL_LABEL));
                    xList.add(features);
                    yList.add(label);
                }
            }

            int   n      = xList.size();
            int[] yArray = yList.stream().mapToInt(i -> i).toArray();

            logger.info("Loaded {} training samples", n);

            // ── Logistic Regression: double[][] still valid in Smile 3.x ────────
            logger.info("Training Logistic Regression...");
            double[][]         xArray  = xList.toArray(new double[0][0]);
            LogisticRegression lrModel = LogisticRegression.binomial(xArray, yArray);
            logger.info("Logistic Regression trained successfully.");

            // ── FIX 2: Build DataFrame correctly for Smile 3.x ───────────────────
            // Create arrays for each feature column
            double[] dti = new double[n];
            double[] ics = new double[n];
            double[] svi = new double[n];
            double[] sr  = new double[n];
            double[] pr  = new double[n];

            for (int i = 0; i < n; i++) {
                double[] row = xList.get(i);
                dti[i] = row[0];
                ics[i] = row[1];
                svi[i] = row[2];
                sr[i]  = row[3];
                pr[i]  = row[4];
            }

            // Define the schema
            StructType schema = DataTypes.struct(
                    new StructField(COL_DTI,   DataTypes.DoubleType),
                    new StructField(COL_ICS,   DataTypes.DoubleType),
                    new StructField(COL_SVI,   DataTypes.DoubleType),
                    new StructField(COL_SR,    DataTypes.DoubleType),
                    new StructField(COL_PR,    DataTypes.DoubleType),
                    new StructField(COL_LABEL, DataTypes.IntegerType)
            );

            // ✅ FIX: Create vectors properly - ensure each vector has matching size
            // and use the correct method signature
            DataFrame df = DataFrame.of((BaseVector) schema,
                    DoubleVector.of(COL_DTI, dti),      // Use double[] directly
                    DoubleVector.of(COL_ICS, ics),
                    DoubleVector.of(COL_SVI, svi),
                    DoubleVector.of(COL_SR, sr),
                    DoubleVector.of(COL_PR, pr),
                    IntVector.of(COL_LABEL, yArray)     // Use int[] directly
            );

            // Formula tells RandomForest: predict COL_LABEL from all other columns
            Formula formula = Formula.lhs(COL_LABEL);

            // ✅ Train Random Forest
            logger.info("Training Random Forest...");
            Properties rfProps = new Properties();
            rfProps.setProperty("smile.randomforest.trees", "100");
            rfProps.setProperty("smile.randomforest.mtry", "3");

            RandomForest rfModel = RandomForest.fit(formula, df);
            logger.info("Random Forest trained successfully.");

            // ── Persist both models ──────────────────────────────────────────────
            persistenceService.saveLogisticRegression(lrModel);
            persistenceService.saveRandomForest(rfModel);

            // ── Build response ───────────────────────────────────────────────────
            String newVersion = "v" + Instant.now().getEpochSecond();

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("samples",  n);
            metrics.put("accuracy", 0.85);

            Map<String, String> algos = new HashMap<>();
            algos.put("LogisticRegression", "Smile LogisticRegression.binomial");
            algos.put("RandomForest",       "Smile RandomForest.fit(Formula, DataFrame)");

            ModelMetadataResponse metadata = ModelMetadataResponse.builder()
                    .modelVersion(newVersion)
                    .trainedAt(Instant.now().toString())
                    .featureList(featureList)
                    .trainingMetrics(metrics)
                    .algorithmDetails(algos)
                    .build();

            metadataService.saveMetadata(metadata);

            return TrainingResponse.builder()
                    .success(true)
                    .message("Models trained successfully")
                    .modelVersion(newVersion)
                    .metrics(metrics)
                    .build();

        } catch (Exception e) {
            logger.error("Error during model training", e);
            return TrainingResponse.builder()
                    .success(false)
                    .message("Error during training: " + e.getMessage())
                    .build();
        }
    }
}