package com.onboarding.accountservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.*;

import java.util.Base64;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.service.username}")
    private String authUsername;

    @Value("${auth.service.password}")
    private String authPassword;

    @GetMapping("/test-auth")
    public String testAuthConnection() {
        StringBuilder result = new StringBuilder();
        String baseUrl = authServiceUrl.endsWith("/") ? authServiceUrl : authServiceUrl + "/";
        String testEndpoint = "api/customers/2";
        String url = baseUrl + testEndpoint;
        
        result.append("=== Testing Auth Service Connection ===\n");
        result.append(String.format("URL: %s\n", url));
        result.append(String.format("Username: %s\n", authUsername));
        result.append("Password: ********\n\n");
        
        // Test 1: Try without any authentication
        result.append("=== Test 1: No Authentication ===\n");
        result.append(testEndpoint(url, null)).append("\n\n");
        
        // Test 2: Try with basic authentication
        result.append("=== Test 2: Basic Authentication ===\n");
        String basicAuth = createBasicAuthHeader(authUsername, authPassword);
        result.append(testEndpoint(url, basicAuth)).append("\n\n");
        
        // Test 3: Try with different authentication methods if needed
        result.append("=== Test 3: Different Authentication Methods ===\n");
        result.append(testWithDifferentAuthMethods(url)).append("\n");
        
        return result.toString();
    }
    
    private String testEndpoint(String url, String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Testing endpoint: {} with headers: {}", url, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                protected boolean hasError(HttpStatusCode statusCode) {
                    return false; // Don't throw exceptions for error status codes
                }
            });
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
                
            return String.format("Status: %d %s\n" +
                              "Headers: %s\n" +
                              "Response: %s",
                    response.getStatusCode().value(),
                    response.getStatusCode().toString(),
                    response.getHeaders().entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining("\n  ")),
                    response.getBody() != null ? response.getBody() : "[No body]");
                    
        } catch (Exception e) {
            return String.format("Error: %s\n" +
                              "Message: %s",
                    e.getClass().getSimpleName(),
                    e.getMessage());
        }
    }
    
    private String testWithDifferentAuthMethods(String url) {
        StringBuilder result = new StringBuilder();
        
        // Test with different variations of the URL
        String[] urlVariations = {
            url,
            url.replace("http://", "http://" + authUsername + ":" + authPassword + "@"),
            url + "?username=" + authUsername + "&password=" + authPassword
        };
        
        for (int i = 0; i < urlVariations.length; i++) {
            result.append(String.format("\nTest %d: %s\n", i + 1, urlVariations[i]));
            result.append(testEndpoint(urlVariations[i], null)).append("\n");
        }
        
        return result.toString();
    }
    
    private String createBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
