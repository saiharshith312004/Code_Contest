package com.onboarding.accountservice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onboarding.accountservice.dto.auth.JwtRequest;
import com.onboarding.accountservice.dto.auth.JwtResponse;
import org.springframework.http.ResponseEntity;

public interface AuthServiceClient {

    ResponseEntity<JwtResponse> login(JwtRequest request) throws JsonProcessingException;

    ResponseEntity<?> getCurrentUser(String token);

    ResponseEntity<String> healthCheck();
}
