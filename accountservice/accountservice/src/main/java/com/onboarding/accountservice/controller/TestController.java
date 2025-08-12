package com.onboarding.accountservice.controller;

import com.onboarding.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@ConditionalOnProperty(name = "controllers.test.enabled", havingValue = "true")
public class TestController {

    private final AccountService accountService;

    @PostMapping("/create-account/{customerId}")
    public ResponseEntity<String> testAccountCreation(@PathVariable Long customerId) {
        log.info("Test endpoint called to create account for customer: {}", customerId);
        try {
            accountService.createAccountForCustomer(customerId);
            return ResponseEntity.ok("Account creation initiated for customer: " + customerId);
        } catch (Exception e) {
            log.error("Error in test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error creating account: " + e.getMessage());
        }
    }
}
