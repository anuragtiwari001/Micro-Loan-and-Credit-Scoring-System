package com.gla.document_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.gla.document_service",  // this service
        "com.gla.common_service"     // picks up JwtFilter, CorsConfig
})
public class DocumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
        System.out.println("DocumentServiceApplication started");
    }
}