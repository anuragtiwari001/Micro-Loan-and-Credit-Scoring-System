package com.gla.document_service.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "dljuhk5cj",
                "api_key", "875966687349429",
                "api_secret", "N8E94I5DXsU07kvBE5Q8E1lWiGM"
        ));
    }
}