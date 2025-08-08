package com.onboarding.authservice.service;

import java.util.List;

import com.onboarding.authservice.dto.JwtRequest;
import com.onboarding.authservice.dto.JwtResponse;
import com.onboarding.authservice.dto.RegisterRequest;
import com.onboarding.authservice.dto.RegisterResponse;  // <-- import the new DTO
import com.onboarding.authservice.model.Customer;

public interface AuthService {
    JwtResponse login(JwtRequest request);
    RegisterResponse register(RegisterRequest request);  // <-- update return type here
    List<Customer> getAllCustomers();
}
