package com.onboarding.authservice.controller;

import com.onboarding.authservice.dto.*;
import com.onboarding.authservice.exception.InvalidTwoFactorCodeException;
import com.onboarding.authservice.model.Customer;
import com.onboarding.authservice.model.User;
import com.onboarding.authservice.repository.UserRepository;
import com.onboarding.authservice.service.AuthService;
import com.onboarding.authservice.service.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
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
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtRequest request, HttpServletResponse response) {
        try {
            JwtResponse jwtResponse = authService.login(request);

            // Create HttpOnly cookie for JWT
            Cookie cookie = new Cookie("JWT_TOKEN", jwtResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);  // set to true if using HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day expiration
            response.addCookie(cookie);

            // Optionally, you can still return the token in the body
            return ResponseEntity.ok(jwtResponse);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (InvalidTwoFactorCodeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid two-factor authentication code");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
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

        Customer customer = user.getCustomer(); // assuming User ↔ Customer mapping exists

        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer profile not found");
        }

        CustomerDashboardResponse response = CustomerDashboardResponse.builder()
                .username(user.getUsername())
                .fullName(customer.getFullName())
                .dob(customer.getDob())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .pan(customer.getPan())
                .aadhaar(customer.getAadhaar())
                .customerId(customer.getCustomerId())  // add this line
                .build();



        return ResponseEntity.ok(response);
    }

}
