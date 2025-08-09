package com.onboarding.accountservice.service;

import com.onboarding.accountservice.client.AuthServiceClient;
import com.onboarding.accountservice.dto.auth.JwtRequest;
import com.onboarding.accountservice.dto.auth.JwtResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AuthService {

    private final AuthServiceClient authServiceClient;
    private final GoogleAuthenticatorService googleAuthenticatorService;
    private final String serviceUsername;
    private final String servicePassword;
    private final String serviceTotpSecret;
    
    private String currentToken;
    private long tokenExpiryTime = 0;
    
    public AuthService(AuthServiceClient authServiceClient, 
                      GoogleAuthenticatorService googleAuthenticatorService,
                      @Value("${auth.service.username}") String serviceUsername,
                      @Value("${auth.service.password}") String servicePassword,
                      @Value("${auth.service.2fa.secret}") String serviceTotpSecret) {
        this.authServiceClient = authServiceClient;
        this.googleAuthenticatorService = googleAuthenticatorService;
        this.serviceUsername = serviceUsername;
        this.servicePassword = servicePassword;
        this.serviceTotpSecret = serviceTotpSecret;
        
        log.info("AuthService initialized with username: {}", serviceUsername);
        log.debug("TOTP secret configured: {}", serviceTotpSecret != null ? "[PROVIDED]" : "[MISSING]");
    }
    
    @Retryable(value = {FeignException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public String getAuthToken() {
        if (isTokenValid()) {
            log.debug("Using cached token (expires at: {})", new java.util.Date(tokenExpiryTime));
            return currentToken;
        }
        
        try {
            // Generate TOTP code for 2FA
            String twoFaCode = googleAuthenticatorService.generateTotpCode(serviceTotpSecret);
            log.info("Generated TOTP code: {}", twoFaCode);
            log.debug("Using TOTP secret: {}", serviceTotpSecret);
            
            // Create login request with service account credentials and 2FA code
            JwtRequest request = new JwtRequest(
                serviceUsername, // service account username from properties
                servicePassword, // service account password from properties
                twoFaCode       // generated TOTP code
            );
            
            log.info("Sending login request to Auth Service for user: {}", serviceUsername);
            log.debug("Request details: username={}, password={}, totpCode={}", 
                    serviceUsername, 
                    servicePassword != null ? "[PROVIDED]" : "[MISSING]", 
                    twoFaCode);
            
            try {
                ResponseEntity<JwtResponse> response = authServiceClient.login(request);
                
                log.info("Received response from Auth Service - Status: {}", response.getStatusCode());
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    currentToken = "Bearer " + response.getBody().getToken();
                    // Set token expiry to 50 minutes from now (JWT tokens typically expire in 1 hour)
                    tokenExpiryTime = System.currentTimeMillis() + (50 * 60 * 1000);
                    log.info("Successfully obtained authentication token. Expires at: {}", 
                            new java.util.Date(tokenExpiryTime));
                    return currentToken;
                } else {
                    log.error("Failed to obtain authentication token. Status: {}, Response: {}", 
                             response.getStatusCode(), 
                             response.getBody());
                    throw new RuntimeException("Failed to obtain authentication token. Status: " + response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                log.error("HTTP error during authentication. Status: {}, Response: {}", 
                         e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new RuntimeException("Authentication failed with status: " + e.getStatusCode(), e);
            }
            
        } catch (HttpClientErrorException e) {
            log.error("HTTP error while authenticating with Auth Service. Status: {}, Response: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Authentication failed: " + e.getStatusText(), e);
        } catch (JsonProcessingException e) {
            log.error("JSON processing error while authenticating with Auth Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process authentication response", e);
        } catch (FeignException e) {
            log.error("Feign error while authenticating with Auth Service. Status: {}, Body: {}", 
                     e.status(), e.contentUTF8(), e);
            throw new RuntimeException("Authentication service unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error while authenticating with Auth Service", e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    public boolean isTokenValid() {
        return currentToken != null && !currentToken.isEmpty() && 
               System.currentTimeMillis() < tokenExpiryTime;
    }
    
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Testing connection to Auth Service at: {}", 
                    authServiceClient.toString().contains("RestTemplate") ? "Using RestTemplate" : "Using Feign Client");
                
                // Test public endpoint
                log.info("Testing public health endpoint...");
                ResponseEntity<String> healthResponse = authServiceClient.healthCheck();
                boolean publicEndpointOk = healthResponse.getStatusCode().is2xxSuccessful();
                log.info("Public health endpoint status: {} - {}", 
                        healthResponse.getStatusCodeValue(), 
                        healthResponse.getBody());
                
                // Only test secured endpoint if public endpoint is working
                boolean securedEndpointOk = false;
                if (publicEndpointOk) {
                    try {
                        log.info("Testing secured endpoint...");
                        String token = getAuthToken();
                        if (token != null && !token.isEmpty()) {
                            log.info("Obtained JWT token, testing secured endpoint...");
                            ResponseEntity<?> meResponse = authServiceClient.getCurrentUser(token);
                            securedEndpointOk = meResponse.getStatusCode().is2xxSuccessful();
                            log.info("Secured endpoint status: {} - {}", 
                                    meResponse.getStatusCodeValue(),
                                    meResponse.getBody());
                        } else {
                            log.warn("Failed to obtain JWT token for testing secured endpoint");
                        }
                    } catch (Exception e) {
                        log.error("Error testing secured endpoint: {}", e.getMessage(), e);
                    }
                }
                
                log.info("Auth Service connection test - Public endpoint: {}, Secured endpoint: {}", 
                        publicEndpointOk ? "OK" : "FAILED",
                        !publicEndpointOk ? "SKIPPED (public endpoint failed)" : 
                        (securedEndpointOk ? "OK" : "FAILED"));
                
                return publicEndpointOk && (securedEndpointOk || !publicEndpointOk);
                
            } catch (Exception e) {
                log.error("Error testing Auth Service connection: {}", e.getMessage(), e);
                return false;
            }
        });
    }
}
