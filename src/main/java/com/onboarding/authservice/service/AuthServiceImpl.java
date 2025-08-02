package com.onboarding.authservice.service;
import com.onboarding.authservice.dto.JwtRequest;
import com.onboarding.authservice.dto.JwtResponse;
import com.onboarding.authservice.dto.RegisterRequest;
import com.onboarding.authservice.model.AuditLog;
import com.onboarding.authservice.model.Customer;
import com.onboarding.authservice.model.User;
import com.onboarding.authservice.repository.AuditLogRepository;
import com.onboarding.authservice.repository.CustomerRepository;
import com.onboarding.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    @Autowired
    private CustomerRepository customerRepository1;
    @Override
    public List<Customer> getAllCustomers() {
    	return customerRepository.findAll();
    }
    
    @Override
    public JwtResponse login(JwtRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));
        String token = jwtUtil.generateToken(user.getUsername(),user.getRole(),user.getUserId());
        AuditLog log = AuditLog.builder()
                .userId(user.getUserId())
                .action("LOGIN")
                .entityType("USER")
                .entityId(user.getUserId())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
        return JwtResponse.builder()
                .token(token)
                .role(user.getRole())
                .username(user.getUsername())
                .build();
    }
    @Override
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        // Save user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toUpperCase())
                .build();
        User savedUser = userRepository.save(user);
        // Save customer only if role is CUSTOMER
        if (user.getRole().equals("CUSTOMER")) {
            Customer customer = Customer.builder()
                    .fullName(request.getFullName())
                    .dob(request.getDob())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .aadhaar(request.getAadhaar())
                    .pan(request.getPan())
                    .address(request.getAddress())
                    .user(savedUser)
                    .build();
            customerRepository.save(customer);
        }
        // Audit log
        AuditLog log = AuditLog.builder()
                .userId(savedUser.getUserId())
                .action("REGISTER")
                .entityType("USER")
                .entityId(savedUser.getUserId())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
        return "User registered successfully!";
    }
}






