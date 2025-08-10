package com.onboarding.customer_onboarding.kyc.controller;


import com.onboarding.customer_onboarding.model.Customer;
import com.onboarding.customer_onboarding.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kyc/admin")
public class KycAdminController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/all")
    public ResponseEntity<Map<String, List<Customer>>> getAllKycRequests() {
        List<Customer> all = customerRepository.findAll();

        Map<String, List<Customer>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> Optional.ofNullable(c.getKycStatus()).orElse("PENDING").toUpperCase()));

        return ResponseEntity.ok(grouped);
    }
}
