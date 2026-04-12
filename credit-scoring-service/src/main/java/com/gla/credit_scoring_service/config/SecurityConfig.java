package com.gla.credit_scoring_service.config;

import com.gla.common_service.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ✅ Disable CSRF (stateless APIs)
                .csrf(csrf -> csrf.disable())

                // ✅ Make app STATELESS (VERY IMPORTANT for JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // 🔥 ADMIN APIs
                        .requestMatchers("/api/v1/credit/admin/**")
                        .hasRole("ADMIN")

                        // 🔥 USER + ADMIN
                        .requestMatchers("/api/v1/credit/score")
                        .hasAnyRole("USER", "ADMIN")

                        // 🔥 (OPTIONAL) allow health check / test
                        // .requestMatchers("/actuator/**").permitAll()

                        // ❗ everything else secured
                        .anyRequest().authenticated()
                )

                // ❌ Disable default Spring login
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())

                // ✅ Add JWT filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


//package com.gla.credit_scoring_service.config;
//import com.gla.common_service.security.JwtFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//@Configuration
//public class SecurityConfig {
//    private final JwtFilter jwtFilter;
//    public SecurityConfig(JwtFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//
//                .authorizeHttpRequests(auth -> auth
//
//                        // ✅ ALLOW ADMIN API
//                        .requestMatchers("/api/v1/credit/admin/**").hasRole("ADMIN")
//
//                        // ✅ USER API
//                        .requestMatchers("/api/v1/credit/score").hasAnyRole("USER", "ADMIN")
//
//                        // ✅ everything else
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(httpBasic -> httpBasic.disable())
//                .formLogin(form -> form.disable())
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}