package com.onboarding.authservice.controller;
import com.onboarding.authservice.dto.JwtRequest;
import com.onboarding.authservice.dto.JwtResponse;
import com.onboarding.authservice.dto.RegisterRequest;
import com.onboarding.authservice.dto.UserResponse;
import com.onboarding.authservice.model.Customer;
import com.onboarding.authservice.model.User;
import com.onboarding.authservice.repository.UserRepository;
import com.onboarding.authservice.service.AuthService;
import com.onboarding.authservice.service.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = authService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateUser(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(new UserResponse(user.getUsername(), user.getRole()));
    }
}