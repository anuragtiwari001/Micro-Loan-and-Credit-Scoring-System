FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY target/ml-model-service-*.jar ml-model-service.jar

# Create models directory
RUN mkdir -p /app/models

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ml-model-service.jar"]
