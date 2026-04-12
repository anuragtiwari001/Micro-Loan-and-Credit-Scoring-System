package com.gla.mlmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ModelPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ModelPersistenceService.class);

    @Value("${app.models.dir}")
    private String modelDir;

    @Value("${app.models.logistic-regression-file}")
    private String lrFileName;

    @Value("${app.models.random-forest-file}")
    private String rfFileName;

    public void saveModel(Object model, String fileName) throws IOException {
        File dir = new File(modelDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File modelFile = new File(dir, fileName);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelFile))) {
            oos.writeObject(model);
            logger.info("Saved model to {}", modelFile.getAbsolutePath());
        }
    }

    public Object loadModel(String fileName) throws IOException, ClassNotFoundException {
        File modelFile = new File(modelDir, fileName);
        if (!modelFile.exists()) {
            throw new FileNotFoundException("Model file not found: " + modelFile.getAbsolutePath());
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile))) {
            return ois.readObject();
        }
    }

    public void saveLogisticRegression(Object lrModel) throws IOException {
        saveModel(lrModel, lrFileName);
    }

    public void saveRandomForest(Object rfModel) throws IOException {
        saveModel(rfModel, rfFileName);
    }

    public Object loadLogisticRegression() throws IOException, ClassNotFoundException {
        return loadModel(lrFileName);
    }

    public Object loadRandomForest() throws IOException, ClassNotFoundException {
        return loadModel(rfFileName);
    }
}
