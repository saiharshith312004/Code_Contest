package com.onboarding.authservice.service;
import java.util.List;

import com.onboarding.authservice.dto.JwtRequest;
import com.onboarding.authservice.dto.JwtResponse;
import com.onboarding.authservice.dto.RegisterRequest;
import com.onboarding.authservice.model.Customer;
public interface AuthService {
    JwtResponse login(JwtRequest request);
    String register(RegisterRequest request);
	List<Customer> getAllCustomers();
}