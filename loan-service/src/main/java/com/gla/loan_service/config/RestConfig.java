package com.gla.loan_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate for loan-service.
 *
 * loan-service calls three downstream services:
 *   credit-scoring-service  — to fetch credit score on loan apply
 *   document-service        — to fetch KYC documents for admin dashboard
 *   user-service            — to resolve email → userId for credit admin call
 *
 * TIMEOUTS:
 *   connectTimeout = 3s  — aggressive; if a service is down, fail fast
 *   readTimeout    = 8s  — credit admin call triggers a full ML pipeline,
 *                          so slightly longer read timeout is appropriate
 *
 * Each failed call is caught individually in LoanService — a timeout on
 * one downstream service does NOT crash the entire dashboard response.
 */
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(8_000);
        return new RestTemplate(factory);
    }
}