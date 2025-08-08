package com.onboarding.authservice.service;
import com.onboarding.authservice.dto.JwtRequest;
import com.onboarding.authservice.dto.JwtResponse;
import com.onboarding.authservice.dto.RegisterRequest;
import com.onboarding.authservice.dto.RegisterResponse;
import com.onboarding.authservice.model.AuditLog;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
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


    private String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        return "otpauth://totp/"
                + issuer + ":" + account
                + "?secret=" + secretKey
                + "&issuer=" + issuer;
    }


    @Autowired
    private CustomerRepository customerRepository1;
    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public JwtResponse login(JwtRequest request) {
        // 1. Authenticate username/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Retrieve the user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        // 3. Validate 2FA code using stored secret
        if (user.getTwoFaSecret() == null) {
            throw new RuntimeException("2FA not configured for this user");
        }

        com.warrenstrange.googleauth.GoogleAuthenticator gAuth = new com.warrenstrange.googleauth.GoogleAuthenticator();
        boolean isCodeValid = gAuth.authorize(user.getTwoFaSecret(), Integer.parseInt(request.getCode()));

        if (!isCodeValid) {
            throw new RuntimeException("Invalid 2FA code");
        }

        // 4. Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getUserId());

        // 5. Log audit
        AuditLog log = AuditLog.builder()
                .userId(user.getUserId())
                .action("LOGIN")
                .entityType("USER")
                .entityId(user.getUserId())
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);

        // 6. Return token
        return JwtResponse.builder()
                .token(token)
                .role(user.getRole())
                .username(user.getUsername())
                .build();
    }@Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toUpperCase())
                .twoFaSecret(secret)
                .build();

        User savedUser = userRepository.save(user);

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

        auditLogRepository.save(AuditLog.builder()
                .userId(savedUser.getUserId())
                .action("REGISTER")
                .entityType("USER")
                .entityId(savedUser.getUserId())
                .timestamp(LocalDateTime.now())
                .build());

        // URL encode username & secret in case they contain special characters
        String usernameEncoded = java.net.URLEncoder.encode(savedUser.getUsername(), java.nio.charset.StandardCharsets.UTF_8);
        String secretEncoded = java.net.URLEncoder.encode(secret, java.nio.charset.StandardCharsets.UTF_8);

        // Generate the otpauth URI
        String otpAuthUrl = String.format("otpauth://totp/YourAppName:%s?secret=%s&issuer=YourAppName", usernameEncoded, secretEncoded);

        // Generate the QR code link using qrserver.com
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + java.net.URLEncoder.encode(otpAuthUrl, java.nio.charset.StandardCharsets.UTF_8);

        return RegisterResponse.builder()
                .message("User registered successfully. Setup 2FA using the secret or QR code.")
                .qrCodeUrl(qrCodeUrl)
                .build();
    }

}






