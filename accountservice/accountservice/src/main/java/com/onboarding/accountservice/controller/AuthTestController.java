package com.onboarding.accountservice.controller;

import com.onboarding.accountservice.client.AuthServiceClient;
import com.onboarding.accountservice.dto.auth.JwtRequest;
import com.onboarding.accountservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for testing the Auth Service integration.
 * This is a test controller and should be disabled in production.
 */
@RestController
@RequestMapping("/api/test/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthTestController {

    private final AuthService authService;
    private final AuthServiceClient authServiceClient;

    /**
     * Test endpoint to verify Auth Service connectivity
     */
    @GetMapping("/test-connection")
    public ResponseEntity<String> testAuthServiceConnection() {
        try {
            boolean isConnected = authService.testConnection().get();
            return isConnected ? 
                ResponseEntity.ok("Successfully connected to Auth Service") :
                ResponseEntity.internalServerError().body("Failed to connect to Auth Service");
        } catch (Exception e) {
            log.error("Error testing Auth Service connection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Error testing Auth Service connection: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint to test authentication with Auth Service
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest request) {
        try {
            String token = authService.getAuthToken();
            return ResponseEntity.ok("Successfully authenticated. Token: " + token);
        } catch (Exception e) {
            log.error("Error authenticating with Auth Service: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Error authenticating with Auth Service: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint to test secured endpoint with JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String token = authService.getAuthToken();
            return authServiceClient.getCurrentUser("Bearer " + token);
        } catch (Exception e) {
            log.error("Error accessing secured endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Error accessing secured endpoint: " + e.getMessage());
        }
    }
}
