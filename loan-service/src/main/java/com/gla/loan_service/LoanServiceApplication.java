package com.gla.loan_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.gla.loan_service",    // this service
        "com.gla.common_service"   // picks up JwtFilter, CorsConfig
})
public class LoanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
        System.out.println("LoanServiceApplication started");
    }
}