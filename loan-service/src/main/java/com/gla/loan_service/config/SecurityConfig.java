package com.gla.loan_service.config;

import com.gla.common_service.security.JwtFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for loan-service (:8085).
 *
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  CRITICAL BUG FIXED                                                  ║
 * ║  Original code had:                                                   ║
 * ║    .requestMatchers("/api/v1/loan/**").permitAll()                    ║
 * ║  This allowed ANY unauthenticated request to POST /loan/apply,        ║
 * ║  GET /loan/dashboard, etc. — a serious security vulnerability.        ║
 * ║  Fixed to .authenticated() — all loan endpoints require a valid JWT.  ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * ROUTE RULES:
 *   /api/v1/loan/**    → authenticated (USER or ADMIN)
 *   /api/v1/admin/**   → ADMIN role only
 *   everything else    → authenticated
 *
 * SESSION: STATELESS — JWT only.
 */
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ FIXED: was .permitAll() — now requires valid JWT
                        .requestMatchers("/api/v1/loan/**")
                        .authenticated()

                        // Admin-only routes (Spring checks ROLE_ADMIN authority)
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

//package com.gla.loan_service.config;
//
//import com.gla.common_service.security.JwtFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.http.SessionCreationPolicy;
//
//@Configuration
//public class SecurityConfig {
//
//    private final JwtFilter jwtFilter;
//
//    public SecurityConfig(JwtFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authorizeHttpRequests(auth -> auth
//
//                        // PUBLIC
//                        .requestMatchers("/api/v1/loan/**").permitAll()
//
//                        // ADMIN ONLY
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//
//                        // EVERYTHING ELSE
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtFilter,
//                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}