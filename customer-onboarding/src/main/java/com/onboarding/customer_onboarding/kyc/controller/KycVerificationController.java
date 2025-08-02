package com.onboarding.customer_onboarding.kyc.controller;

import com.onboarding.customer_onboarding.kyc.model.KycVerification;
import com.onboarding.customer_onboarding.kyc.service.KycService;
import com.onboarding.customer_onboarding.kyc.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/kyc")
public class KycVerificationController {
    @Autowired
    private KycService kycService;
    @Autowired
    private JwtUtil jwtUtil;

    @PutMapping("/verify/{customerId}/{documentId}")
    public ResponseEntity<String> verifyKycDocument(
            @PathVariable Long customerId,
            @PathVariable Long documentId,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }
        String jwt = authHeader.substring(7);
        String adminUsername = jwtUtil.extractUsername(jwt);
        Long adminId = jwtUtil.extractUserId(jwt); 
        kycService.verifyKycDocument(customerId, documentId, status, remarks, adminUsername, adminId);
        return ResponseEntity.ok("KYC document verification status updated");
    }
}
