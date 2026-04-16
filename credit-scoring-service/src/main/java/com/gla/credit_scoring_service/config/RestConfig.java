package com.gla.credit_scoring_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate bean for credit-scoring-service.
 *
 * TIMEOUTS:
 *   connectTimeout = 3 seconds  — time to establish TCP connection
 *   readTimeout    = 5 seconds  — time to wait for full response
 *
 * WHY THIS MATTERS:
 *   getCreditScore() calls both data-service and ml-model-service.
 *   Without timeouts, a slow ML model call would block the thread indefinitely.
 *   With 5s readTimeout, CreditScoreService catches the timeout exception
 *   and returns the fallback score (650) instead of hanging.
 *
 * PRODUCTION UPGRADE:
 *   Replace SimpleClientHttpRequestFactory with Apache HttpClient or
 *   OkHttp for connection pooling and better resilience.
 */
@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);   // 3 seconds
        factory.setReadTimeout(5_000);      // 5 seconds
        return new RestTemplate(factory);
    }
}