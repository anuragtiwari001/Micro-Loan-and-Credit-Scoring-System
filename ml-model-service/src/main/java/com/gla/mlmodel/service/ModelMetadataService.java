package com.gla.mlmodel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gla.mlmodel.dto.ModelMetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ModelMetadataService {
    private static final Logger logger = LoggerFactory.getLogger(ModelMetadataService.class);

    private final ObjectMapper objectMapper;

    @Value("${app.models.dir}")
    private String modelDir;

    @Value("${app.models.metadata-file}")
    private String metadataFileName;

    private ModelMetadataResponse currentMetadata;

    public ModelMetadataService() {
        this.objectMapper = new ObjectMapper();
    }

    public void saveMetadata(ModelMetadataResponse metadata) {
        File dir = new File(modelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File metaFile = new File(dir, metadataFileName);
        try {
            objectMapper.writeValue(metaFile, metadata);
            this.currentMetadata = metadata;
            logger.info("Saved model metadata to {}", metaFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save model metadata", e);
        }
    }

    public ModelMetadataResponse loadMetadata() {
        File metaFile = new File(modelDir, metadataFileName);
        if (metaFile.exists()) {
            try {
                this.currentMetadata = objectMapper.readValue(metaFile, ModelMetadataResponse.class);
                return currentMetadata;
            } catch (IOException e) {
                logger.error("Failed to load model metadata from {}", metaFile.getAbsolutePath(), e);
            }
        }
        return null;
    }

    public ModelMetadataResponse getCurrentMetadata() {
        return currentMetadata;
    }
}
