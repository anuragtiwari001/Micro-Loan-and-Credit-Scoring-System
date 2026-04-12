package com.gla.mlmodel.bootstrap;

import com.gla.mlmodel.service.PredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ModelLoaderBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ModelLoaderBootstrap.class);
    
    private final PredictionService predictionService;

    public ModelLoaderBootstrap(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application starting: Initializing ML model payload...");
        predictionService.reloadModels();
    }
}
