package com.gla.data_service.config;

import com.gla.common_service.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for data-service (:8082).
 *
 * ALL endpoints require a valid JWT.
 * There are no public endpoints on the data-service — even GET /data/get
 * requires authentication (the credit-scoring-service forwards its token).
 *
 * SESSION: STATELESS.
 */
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // JwtFilter is a @Component in common — Spring auto-wires it here
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

////package com.gla.data_service.config;
////
////import com.gla.common_service.security.JwtFilter;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////
////@Configuration
////public class SecurityConfig {
////
////    // ✅ FIX: Inject JwtFilter as a Spring-managed bean (registered by @Component in common)
////    // Do NOT create a new JwtFilter() — that makes a second unmanaged instance
////    // which Spring Security may apply TWICE causing 403s on valid tokens
////    private final JwtFilter jwtFilter;
////
////    public SecurityConfig(JwtFilter jwtFilter) {
////        this.jwtFilter = jwtFilter;
////    }
////
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////
////        http
////                .csrf(csrf -> csrf.disable())
////
////                // ✅ STATELESS — critical for JWT to work correctly
////                .sessionManagement(session ->
////                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////                )
////
////                .authorizeHttpRequests(auth -> auth
////                        // 🔐 ALL APIs require JWT — ADMIN can access any email's data
////                        .anyRequest().authenticated()
////                )
////
////                // ❌ Disable default Spring login form
////                .httpBasic(httpBasic -> httpBasic.disable())
////                .formLogin(form -> form.disable())
////
////                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
////
////        return http.build();
////    }
////}
//
//
//package com.gla.data_service.config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import com.gla.common_service.security.JwtFilter;
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public JwtFilter jwtFilter() {
//        return new JwtFilter();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        // 🔥 allow ONLY public endpoints (if any)
//                        //.requestMatchers("/api/v1/data/get").permitAll()  ❌ REMOVE
//
//                        // 🔐 ALL APIs require JWT
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}
//
