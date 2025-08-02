package com.onboarding.authservice.dto;
import java.time.LocalDate;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // Values: "ADMIN" or "CUSTOMER"
    // Only for role=CUSTOMER
    private String fullName;
    private LocalDate dob;
    private String phone;
    private String email;
    private String address;
    private String aadhaar;
    private String pan;
}
