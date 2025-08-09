package com.onboarding.accountservice.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean kycVerified;
    // Add other customer fields as needed
}
