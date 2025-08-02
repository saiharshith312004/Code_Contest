package com.onboarding.customer_onboarding.kyc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminJwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.matches("/api/kyc/verify/\\d+/\\d+.*")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }
            try {
                String token = authHeader.substring(7); // Remove "Bearer " prefix
                String role = jwtUtil.extractRole(token);
                if (!"ADMIN".equals(role)) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("Access denied: Admins only");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid token: " + e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
