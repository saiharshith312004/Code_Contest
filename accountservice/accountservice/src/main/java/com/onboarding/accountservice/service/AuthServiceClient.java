package com.onboarding.accountservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.accountservice.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient implements InitializingBean {

    private final RestTemplateBuilder restTemplateBuilder;
    private RestTemplate restTemplate;
    
    @Value("${auth.service.url}")
    private String authServiceBaseUrl;

    @Value("${auth.service.jwt.token:}")
    private String jwtToken;

    @Override
    public void afterPropertiesSet() {
        this.restTemplate = restTemplateBuilder
            .rootUri(authServiceBaseUrl)
            .defaultHeader("X-Requested-With", "XMLHttpRequest")
            .defaultHeader("Accept", "application/json")
            .build();
            
        log.info("Initialized AuthServiceClient with base URL: {}", authServiceBaseUrl);
    }

    public CustomerDTO getCustomerById(Long customerId) {
        String url = "/api/customers/" + customerId;
        String fullUrl = authServiceBaseUrl + url;
        log.info("Fetching customer details from: {}", fullUrl);
        
        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.debug("Sending GET request to: {}", fullUrl);
            log.debug("Request headers: {}", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            log.debug("Response status: {}", response.getStatusCode());
            log.debug("Response headers: {}", response.getHeaders());
            log.debug("Response body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // If successful, parse the response
                ObjectMapper mapper = new ObjectMapper();
                CustomerDTO customer = mapper.readValue(response.getBody(), CustomerDTO.class);
                
                log.debug("Retrieved customer: {}", customer);
                
                if (!customer.isKycVerified()) {
                    log.warn("Customer with ID {} is not KYC verified", customerId);
                    throw new IllegalStateException("Customer is not KYC verified");
                }
                return customer;
            } else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("Access denied (403) when calling Auth Service. URL: {}", fullUrl);
                log.error("Response headers: {}", response.getHeaders());
                log.error("Response body: {}", response.getBody());
                throw new RuntimeException("Access denied to Auth Service. Please check authentication credentials.");
            } else {
                log.error("Failed to fetch customer details. Status: {}, Response: {}", 
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to fetch customer details. Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling Auth Service: {}", e.getStatusText());
            log.error("Response body: {}", e.getResponseBodyAsString());
            log.error("Response headers: {}", e.getResponseHeaders());
            throw new RuntimeException("HTTP Error communicating with Auth Service: " + e.getStatusText(), e);
        } catch (RestClientException e) {
            log.error("Error calling Auth Service: {}", e.getMessage());
            log.error("Full error details:", e);
            throw new RuntimeException("Error communicating with Auth Service: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    public boolean testConnection() {
        // First test with a public endpoint
        if (!testPublicEndpoint()) {
            log.error("Failed to connect to Auth Service public endpoint");
            return false;
        }
        
        // Then test with secured endpoint
        return testSecuredEndpoint();
    }
    
    private boolean testPublicEndpoint() {
        String url = authServiceBaseUrl + "/api/auth/health";
        log.info("🔍 Testing public endpoint: {}", url);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("✅ Public endpoint response: {}", response.getBody());
            return true;
        } catch (Exception e) {
            log.error("❌ Error accessing public endpoint: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean testSecuredEndpoint() {
        String url = authServiceBaseUrl + "/api/auth/me";
        log.info("🔍 Testing secured endpoint: {}", url);
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.trim());
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            
            log.info("   JWT Token: {}... (length: {})", 
                jwtToken.substring(0, Math.min(20, jwtToken.length())), 
                jwtToken.length());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("🔄 Making authenticated request...");
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            log.info("✅ Secured endpoint response: {}", response.getBody());
            return true;
            
        } catch (HttpClientErrorException e) {
            log.error("❌ Auth Service returned error ({}): {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            log.error("   Response headers: {}", e.getResponseHeaders());
            
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("   Possible issues:");
                log.error("   1. JWT token might be invalid or expired");
                log.error("   2. User might not have required permissions");
                log.error("   3. Auth Service might be expecting different headers");
            }
            return false;
        } catch (Exception e) {
            log.error("❌ Error testing secured endpoint: {}", e.getMessage(), e);
            return false;
        }
    }
}
