package com.gla.credit_scoring_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.gla.credit_scoring_service",  // this service
        "com.gla.common_service"           // picks up JwtFilter, CorsConfig
})
public class CreditScoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditScoringApplication.class, args);
        System.out.println("CreditScoringApplication started");
    }
}

