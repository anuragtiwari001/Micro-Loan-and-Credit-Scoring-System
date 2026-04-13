# GLA Micro Loan ML Model Service

A production-grade Java 17 Spring Boot 3.x microservice using the classical `Smile` machine learning library to perform credit scoring for micro-loans.

## Features Supported
This service revolves strictly around these 5 core engineered variables:
1. `debtToIncomeRatio`
2. `incomeConsistencyScore`
3. `spendingVolatilityIndex`
4. `savingsRate`
5. `paymentRegularity`

*Validation rule:* If only `monthlyIncome`, `monthlyExpenses`, `existingEMIs` are given, the service dynamically engineers the required variables.

## Architecture
- **Dual-Model Ensemble:** Blends probabilities: `0.35 * Logistic Regression` + `0.65 * Random Forest`.
- **Normalization:** Result probability gets transformed tightly into a pseudo FICO 300 to 850 score.
- **Strict Rule-based Fallback:** If the loaded models fail or disk reading breaks, an unkillable manual fallback algorithm is used to provide accurate risk results natively.

## How the Ensemble Works
During training, data is fed to two Java-native SMILE instances (`LogisticRegression` and `RandomForest`). When a prediction is requested:
1. Data array passes into both `.predict()` methods.
2. The `posteriori` probability array allows fetching class '1' probabilities.
3. The probabilities are multiplied by configured Spring properties (`weight-lr` and `weight-rf`) to arrive at the final confidence.

## Feature Importance Explained
As Java Smile extracts specific importance variably, the default standard applies configured business importance weights into the map representation under `featureImportance`.

## Integration with upstream Credit Scoring Service
The upstream service simply hits `POST /predict`. All credit rules, fallbacks, model state, and DTI conversions hide behind this service instance.

## Endpoints
1. `POST /api/v1/model/predict` (Prediction Payload)
2. `POST /api/v1/model/train` (Points to physical path payload)
3. `POST /api/v1/model/reload`
4. `GET /api/v1/model/health`
5. `GET /api/v1/model/metadata`

## Running Locally
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## Docker
```bash
docker build -t ml-model-service .
docker run -p 8080:8080 -v $(pwd)/models:/app/models ml-model-service
```

## Example Payload for /predict
```json
{
  "monthlyIncome": 5000,
  "monthlyExpenses": 2000,
  "existingEMIs": 1000,
  "employmentLengthMonths": 36,
  "missedPaymentsCount": 0
}
```

## Fallback Rules
Triggered when `/reload` fails to find `models/logistic_regression.bin` and `models/random_forest.bin`. Employs weighted coefficients natively to guarantee a response.
