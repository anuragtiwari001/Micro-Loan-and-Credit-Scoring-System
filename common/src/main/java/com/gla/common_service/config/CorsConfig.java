package com.gla.common_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Global CORS configuration shared by ALL services via the common module.
 *
 * Allows requests from the React frontend on localhost:3000 (CRA) and
 * localhost:5173 (Vite) during development.
 *
 * HOW IT WORKS:
 *   Every service imports the `common` module as a Maven dependency.
 *   Spring component-scan picks up this @Configuration automatically.
 *   No per-service CORS setup is needed.
 *
 * PRODUCTION:
 *   Replace the hardcoded origins with your deployed frontend URL,
 *   ideally read from an environment variable:
 *     System.getenv("FRONTEND_URL")
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // ── Allowed frontend origins ──────────────────────────────────────────
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",     // Create React App
                "http://localhost:5173",     // Vite / React + Vite
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));

        // ── Allowed HTTP methods ───────────────────────────────────────────────
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ── Allowed headers ────────────────────────────────────────────────────
        // "Authorization" is critical for JWT; "Content-Type" for JSON + multipart.
        config.setAllowedHeaders(List.of("*"));

        // ── Expose headers to the browser ─────────────────────────────────────
        config.setExposedHeaders(List.of("Authorization"));

        // ── Allow credentials (cookies, Authorization header) ─────────────────
        config.setAllowCredentials(true);

        // ── Pre-flight cache duration ─────────────────────────────────────────
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}