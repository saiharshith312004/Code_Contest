package com.onboarding.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(prefix = "controllers", name = "health.enabled", havingValue = "true", matchIfMissing = false)
public class HealthController {

    @GetMapping(value = "/health", produces = "application/json")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = Map.of(
                "status", "UP",
                "service", "authservice"
        );
        return ResponseEntity.ok(body);
    }
}
