package com.gla.common_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Shared JWT filter injected into every service's Spring Security chain.
 *
 * RESPONSIBILITIES:
 *  1. Extract "Authorization: Bearer <token>" header.
 *  2. Validate the token via JwtUtil.
 *  3. Set Spring SecurityContext with email + ROLE_{role} authority.
 *  4. Store email and role as request attributes so controllers can read them:
 *       String email = (String) request.getAttribute("email");
 *       String role  = (String) request.getAttribute("role");
 *  5. Return 401 JSON on any JWT error instead of leaking stack traces.
 *
 * WIRING:
 *   Each service's SecurityConfig must inject this bean and call:
 *     http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ── No token → let Spring Security decide (public endpoints pass through) ──
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = JwtUtil.extractEmail(token);
            String role  = JwtUtil.extractRole(token);   // "USER" or "ADMIN"

            // Spring Security authority must be prefixed with "ROLE_"
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + role);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email, null, List.of(authority));

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Make email + role directly accessible in controllers
            request.setAttribute("email", email);
            request.setAttribute("role",  role);

        } catch (Exception e) {
            // Return clean 401 — do NOT leak the exception message
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Invalid or expired token\"," +
                            "\"statusCode\":401}");
            return;
        }

        chain.doFilter(request, response);
    }
}

//package com.gla.common_service.security;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//import java.util.List;
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//        System.out.println("🔥 JWT FILTER HIT");
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        try {
//            String token = authHeader.substring(7);
//            String email = JwtUtil.extractEmail(token);
//            String role = JwtUtil.extractRole(token);
//            System.out.println("EMAIL: " + email);
//            System.out.println("ROLE: " + role);
//            // ✅ ROLE FIX (already correct)
//            SimpleGrantedAuthority authority =
//                    new SimpleGrantedAuthority("ROLE_" + role);
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            email,
//                            null,
//                            List.of(authority)
//                    );
//
//            // ✅ ADD DETAILS (IMPORTANT FOR SPRING SECURITY)
//            authentication.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request)
//            );
//
//            // ✅ SET AUTH
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            // ✅ CONTROLLER ACCESS
//            request.setAttribute("email", email);
//            request.setAttribute("role", role);
//
//        } catch (Exception e) {
//            System.out.println("❌ JWT ERROR: " + e.getMessage());
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}