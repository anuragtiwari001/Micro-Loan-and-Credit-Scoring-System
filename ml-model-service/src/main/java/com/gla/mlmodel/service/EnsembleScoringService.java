package com.gla.mlmodel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnsembleScoringService {

    @Value("${app.ensemble.weight-lr}")
    private double lrWeight;

    @Value("${app.ensemble.weight-rf}")
    private double rfWeight;

    public double calculateEnsembleProbability(double lrProbability, double rfProbability) {
        double combined = (lrProbability * lrWeight) + (rfProbability * rfWeight);
        
        // Normalize weights if they don't exactly equal 1
        double totalWeight = lrWeight + rfWeight;
        return combined / totalWeight;
    }
}
