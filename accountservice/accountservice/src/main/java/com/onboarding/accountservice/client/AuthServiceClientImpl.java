package com.onboarding.accountservice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.accountservice.dto.auth.JwtRequest;
import com.onboarding.accountservice.dto.auth.JwtResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AuthServiceClientImpl implements AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthServiceClientImpl(RestTemplate restTemplate, 
                               @Value("${auth.service.url}") String authServiceUrl,
                               ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<JwtResponse> login(JwtRequest request) throws JsonProcessingException {
        String url = authServiceUrl + "/api/auth/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Log the request details
        try {
            log.info("Sending login request to: {}", url);
            log.debug("Request headers: {}", headers);
            log.debug("Request body: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.error("Error logging request: {}", e.getMessage());
        }
        
        HttpEntity<JwtRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            // Use exchange with String.class to capture the raw response
            log.debug("Executing POST request to: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            // If we get here, the request was successful (2xx)
            HttpHeaders responseHeaders = response.getHeaders();
            log.info("Received successful login response with status: {}", response.getStatusCode());
            log.debug("Response headers: {}", responseHeaders);
            log.debug("Response body: {}", response.getBody());
            
            // Convert the response body to JwtResponse
            JwtResponse jwtResponse = objectMapper.readValue(response.getBody(), JwtResponse.class);
            return new ResponseEntity<>(jwtResponse, responseHeaders, response.getStatusCode());
            
        } catch (HttpClientErrorException e) {
            // Log the detailed error response
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            HttpHeaders responseHeaders = e.getResponseHeaders();
            
            log.error("Error during login request to Auth Service. Status: {}", statusCode);
            log.error("Response headers: {}", responseHeaders);
            log.error("Response body: {}", responseBody);
            log.error("Exception details:", e);
            
            if (statusCode.value() == HttpStatus.FORBIDDEN.value()) {
                log.error("Authentication failed. Possible reasons:");
                log.error("1. Invalid username or password");
                log.error("2. Invalid or expired TOTP code");
                log.error("3. User account is locked or disabled");
                log.error("4. Missing or invalid CSRF token");
            }
            
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login request to Auth Service: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ResponseEntity<?> getCurrentUser(String token) {
        String url = authServiceUrl + "/api/auth/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }

    @Override
    public ResponseEntity<String> healthCheck() {
        String url = authServiceUrl + "/api/auth/health";
        return restTemplate.getForEntity(url, String.class);
    }
}
