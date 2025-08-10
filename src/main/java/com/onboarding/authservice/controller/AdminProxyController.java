package com.onboarding.authservice.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminProxyController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String CUSTOMER_SERVICE_BASE = "http://localhost:8090/api/kyc";

    @GetMapping("/all")
    public ResponseEntity<?> getAllRequests() {
        return restTemplate.getForEntity(CUSTOMER_SERVICE_BASE + "/admin/all", Object.class);
    }

    @PutMapping("/{customerId}/approve")
    public ResponseEntity<String> approveKyc(@PathVariable Long customerId) {
        return restTemplate.exchange(
                CUSTOMER_SERVICE_BASE + "/verify/" + customerId + "?status=APPROVED",
                HttpMethod.PUT,
                null,
                String.class
        );
    }

    @PutMapping("/{customerId}/decline")
    public ResponseEntity<String> declineKyc(@PathVariable Long customerId) {
        return restTemplate.exchange(
                CUSTOMER_SERVICE_BASE + "/verify/" + customerId + "?status=DECLINED",
                HttpMethod.PUT,
                null,
                String.class
        );
    }
}
