package com.gla.credit_scoring_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.gla.credit_scoring_service",
                "com.gla.common_service"
        }
)
public class CreditScoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditScoringApplication.class, args);
    }
}
