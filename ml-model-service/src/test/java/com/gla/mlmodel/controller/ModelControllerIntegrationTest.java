package com.gla.mlmodel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gla.mlmodel.dto.PredictionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper mapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/model/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP")); // Models may not be loaded if file doesn't exist
    }

    @Test
    void testPredictionWithFallback() throws Exception {
        PredictionRequest req = PredictionRequest.builder()
                .monthlyIncome(5000.0)
                .monthlyExpenses(2000.0)
                .existingEMIs(1000.0)
                .build();

        mockMvc.perform(post("/api/v1/model/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.modelUsed").value("RULE_BASED_FALLBACK"))
               .andExpect(jsonPath("$.normalizedCreditScore", greaterThan(300)));
    }
}
