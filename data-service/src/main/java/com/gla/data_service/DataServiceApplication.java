package com.gla.data_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.gla.data_service",    // this service's own beans
        "com.gla.common_service"   // picks up JwtFilter, CorsConfig from common
})
public class DataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataServiceApplication.class, args);
        System.out.println("DataServiceApplication started");
    }
}